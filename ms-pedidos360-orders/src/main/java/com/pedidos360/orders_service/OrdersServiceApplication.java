package com.pedidos360.orders_service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.client.RestTemplate;

import com.pedidos360.orders_service.entity.Order;
import com.pedidos360.orders_service.repository.OrderRepository;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class OrdersServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrdersServiceApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    CommandLineRunner initDatabase(OrderRepository repositorio, KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        return args -> {
            repositorio.deleteAll();

            // Pedido 1: Ciclo completo ENTREGADO
            Order p1 = crearPedido(repositorio, kafkaTemplate, objectMapper, "Empresa Alpha S.A.", 450000.0);
            avanzarEstado(repositorio, kafkaTemplate, objectMapper, p1, Order.OrderStatus.ACEPTADO);
            avanzarEstado(repositorio, kafkaTemplate, objectMapper, p1, Order.OrderStatus.EN_PREPARACION);
            avanzarEstado(repositorio, kafkaTemplate, objectMapper, p1, Order.OrderStatus.DESPACHADO);
            avanzarEstado(repositorio, kafkaTemplate, objectMapper, p1, Order.OrderStatus.ENTREGADO);

            // Pedido 2: DESPACHADO
            Order p2 = crearPedido(repositorio, kafkaTemplate, objectMapper, "Retail Sur Ltda.", 1200000.0);
            avanzarEstado(repositorio, kafkaTemplate, objectMapper, p2, Order.OrderStatus.ACEPTADO);
            avanzarEstado(repositorio, kafkaTemplate, objectMapper, p2, Order.OrderStatus.EN_PREPARACION);
            avanzarEstado(repositorio, kafkaTemplate, objectMapper, p2, Order.OrderStatus.DESPACHADO);

            // Pedido 3: EN_PREPARACION
            Order p3 = crearPedido(repositorio, kafkaTemplate, objectMapper, "Juan Pérez", 35000.0);
            avanzarEstado(repositorio, kafkaTemplate, objectMapper, p3, Order.OrderStatus.ACEPTADO);
            avanzarEstado(repositorio, kafkaTemplate, objectMapper, p3, Order.OrderStatus.EN_PREPARACION);

            // Pedido 4: CREADO
            crearPedido(repositorio, kafkaTemplate, objectMapper, "Empresa Alpha S.A.", 89000.0);

            // Pedido 5: ACEPTADO
            Order p5 = crearPedido(repositorio, kafkaTemplate, objectMapper, "María Gómez", 150000.0);
            avanzarEstado(repositorio, kafkaTemplate, objectMapper, p5, Order.OrderStatus.ACEPTADO);

            // Pedido 6: CANCELADO
            Order p6 = crearPedido(repositorio, kafkaTemplate, objectMapper, "Tech Solutions", 540000.0);
            avanzarEstado(repositorio, kafkaTemplate, objectMapper, p6, Order.OrderStatus.CANCELADO);

            // Pedido 7: CREADO
            crearPedido(repositorio, kafkaTemplate, objectMapper, "Retail Sur Ltda.", 230000.0);

            // Pedido 8: DESPACHADO
            Order p8 = crearPedido(repositorio, kafkaTemplate, objectMapper, "Juan Pérez", 12500.0);
            avanzarEstado(repositorio, kafkaTemplate, objectMapper, p8, Order.OrderStatus.ACEPTADO);
            avanzarEstado(repositorio, kafkaTemplate, objectMapper, p8, Order.OrderStatus.EN_PREPARACION);
            avanzarEstado(repositorio, kafkaTemplate, objectMapper, p8, Order.OrderStatus.DESPACHADO);

            // Pedidos adicionales masivos para pruebas de paginación y filtros
            Order p9 = crearPedido(repositorio, kafkaTemplate, objectMapper, "Logística Austral SpA", 850000.0);
            avanzarEstado(repositorio, kafkaTemplate, objectMapper, p9, Order.OrderStatus.ACEPTADO);
            avanzarEstado(repositorio, kafkaTemplate, objectMapper, p9, Order.OrderStatus.ENTREGADO);

            Order p10 = crearPedido(repositorio, kafkaTemplate, objectMapper, "Inversiones Andes", 310000.0);
            avanzarEstado(repositorio, kafkaTemplate, objectMapper, p10, Order.OrderStatus.ACEPTADO);
            avanzarEstado(repositorio, kafkaTemplate, objectMapper, p10, Order.OrderStatus.EN_PREPARACION);

            crearPedido(repositorio, kafkaTemplate, objectMapper, "Comercial Bío Bío", 95000.0);
            
            Order p12 = crearPedido(repositorio, kafkaTemplate, objectMapper, "Distribuidora del Norte", 2100000.0);
            avanzarEstado(repositorio, kafkaTemplate, objectMapper, p12, Order.OrderStatus.CANCELADO);

            System.out.println("✅ Base de datos inicializada con 12 pedidos y timeline completo en Kafka.");
        };
    }

    private Order crearPedido(OrderRepository repositorio, KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper, String customerId, double total) {
        Order order = new Order();
        order.setCustomerId(customerId);
        order.setTotal(total);
        order.setStatus(Order.OrderStatus.CREADO);
        repositorio.save(order);
        publicarEvento(kafkaTemplate, objectMapper, order);
        return order;
    }

    private void avanzarEstado(OrderRepository repositorio, KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper, Order order, Order.OrderStatus nuevoEstado) {
        order.setStatus(nuevoEstado);
        repositorio.save(order);
        publicarEvento(kafkaTemplate, objectMapper, order);
    }

    private void publicarEvento(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper, Order order) {
        Map<String, Object> evento = new HashMap<>();
        evento.put("eventType", "OrderStatusChanged");
        evento.put("orderId", order.getId());
        evento.put("customerId", order.getCustomerId());
        evento.put("status", order.getStatus().name());
        evento.put("total", order.getTotal());
        evento.put("timestamp", Instant.now().toString());

        try {
            String json = objectMapper.writeValueAsString(evento);
            kafkaTemplate.send("orders.events", order.getId().toString(), json);
            kafkaTemplate.send("audit.timeline", order.getId().toString(), json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}