package com.pedidos360.orders_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pedidos360.orders_service.entity.Order;
import com.pedidos360.orders_service.repository.OrderRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final RabbitTemplate rabbitTemplate;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper mapper;
    private final RestTemplate restTemplate;

    @Value("${CATALOG_SERVICE_URL:http://catalog-pedidos360:8082}")
    private String catalogUrl;

    public OrderService(OrderRepository orderRepository, RabbitTemplate rabbitTemplate, KafkaTemplate<String, String> kafkaTemplate, RestTemplate restTemplate) {
        this.orderRepository = orderRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.kafkaTemplate = kafkaTemplate;
        this.restTemplate = restTemplate;
        
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
    }

    public Order createOrder(Order order) {
        order.setStatus(Order.OrderStatus.CREADO);
        if (order.getTotal() == null) {
            order.setTotal(0.0);
        }
        Order savedOrder = orderRepository.save(order);
        
        publicarEventoKafka(savedOrder);
        enviarNotificacionRabbitMQ(savedOrder.getId(), "Su pedido #" + savedOrder.getId() + " ha sido creado.");
        
        return savedOrder;
    }

    public Order updateOrderStatus(Long orderId, Order.OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        if (newStatus == Order.OrderStatus.DESPACHADO && 
            order.getStatus() != Order.OrderStatus.ACEPTADO && 
            order.getStatus() != Order.OrderStatus.EN_PREPARACION) {
            throw new IllegalStateException("Error de negocio: No se puede despachar un pedido que no ha sido aceptado.");
        }

        order.setStatus(newStatus);
        Order updatedOrder = orderRepository.save(order);

        // Regla de Negocio: Descontar stock al ACEPTAR el pedido
        if (newStatus == Order.OrderStatus.ACEPTADO) {
            coordinarDescuentoStock(updatedOrder);
        }

        publicarEventoKafka(updatedOrder);

        if (newStatus == Order.OrderStatus.ACEPTADO || newStatus == Order.OrderStatus.DESPACHADO) {
            enviarNotificacionRabbitMQ(updatedOrder.getId(), "El estado de su pedido #" + updatedOrder.getId() + " cambió a: " + newStatus);
        }

        return updatedOrder;
    }

    private void coordinarDescuentoStock(Order order) {
        try {
            System.out.println("📦 [STOCK] Iniciando descuento de stock para el pedido: " + order.getId());
            Long productId = order.getProductId();
            
            if (productId != null) {
                int quantity = 1;
                String url = catalogUrl + "/api/catalog/products/" + productId + "/reduce-stock?quantity=" + quantity;
                
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                HttpHeaders headers = new HttpHeaders();
                
                if (attributes != null) {
                    HttpServletRequest request = attributes.getRequest();
                    
                    // MODO DETECTIVE: Escaneamos si el BFF nos mandó el token
                    System.out.println("🔍 [DEBUG] Buscando el token JWT en las cabeceras de la petición...");
                    java.util.Enumeration<String> headerNames = request.getHeaderNames();
                    boolean hasAuth = false;
                    
                    while (headerNames != null && headerNames.hasMoreElements()) {
                        String headerName = headerNames.nextElement();
                        if (headerName.toLowerCase().contains("authorization")) {
                            hasAuth = true;
                            String authHeader = request.getHeader(headerName);
                            System.out.println("🔑 [DEBUG] ¡Token JWT encontrado! Pasándolo al Catálogo...");
                            headers.set(HttpHeaders.AUTHORIZATION, authHeader);
                            break;
                        }
                    }
                    
                    if (!hasAuth) {
                        System.err.println("⚠ [FATAL] El microservicio ms-orders NO recibió el token. Tu BFF (o API Gateway) está borrando la cabecera 'Authorization' al enrutar.");
                    }
                }

                HttpEntity<Void> entity = new HttpEntity<>(headers);
                restTemplate.exchange(url, HttpMethod.PUT, entity, Void.class);
                
                System.out.println("✅ [STOCK] Éxito: Se descontó 1 unidad del producto ID " + productId);
            } else {
                System.err.println("⚠ [STOCK] El pedido no tiene un producto asociado.");
            }
        } catch (Exception e) {
            System.err.println("❌ [STOCK] Error al coordinar stock con el catálogo: " + e.getMessage());
        }
    }
    
    private void publicarEventoKafka(Order order) {
        try {
            String eventPayload = mapper.writeValueAsString(order);
            kafkaTemplate.send("orders.events", eventPayload);
            kafkaTemplate.send("audit.timeline", eventPayload);
            System.out.println("🚀 [KAFKA] Evento emitido: Pedido " + order.getId() + " cambió a " + order.getStatus());
        } catch (Exception e) {
            System.err.println("❌ Error al serializar evento Kafka: " + e.getMessage());
        }
    }

    private void enviarNotificacionRabbitMQ(Long orderId, String mensaje) {
        Map<String, Object> envelope = new HashMap<>();
        envelope.put("type", "EMAIL_NOTIFICATION");
        envelope.put("eventId", UUID.randomUUID().toString());
        envelope.put("timestamp", LocalDateTime.now().toString());
        envelope.put("traceId", UUID.randomUUID().toString()); 
        envelope.put("correlationId", orderId.toString());

        Map<String, String> payload = new HashMap<>();
        payload.put("mensaje", mensaje);
        envelope.put("payload", payload);

        rabbitTemplate.convertAndSend("cmd.direct", "email.send", envelope);
    }
}
