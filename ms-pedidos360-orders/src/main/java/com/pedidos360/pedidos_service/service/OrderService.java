package com.pedidos360.pedidos_service.service;

import com.pedidos360.pedidos_service.entity.Pedido;
import com.pedidos360.pedidos_service.messaging.EventEnvelope;
import com.pedidos360.pedidos_service.model.OrderStatus;
import com.pedidos360.pedidos_service.repositorio.PedidoRepositorio;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class OrderService {

    private final PedidoRepositorio orderRepository;
    private final RabbitTemplate rabbitTemplate;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final WebClient webClient;

    public OrderService(PedidoRepositorio orderRepository, 
                        RabbitTemplate rabbitTemplate, 
                        KafkaTemplate<String, Object> kafkaTemplate, 
                        WebClient.Builder webClientBuilder) {
        this.orderRepository = orderRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.kafkaTemplate = kafkaTemplate;
        // Apunta al puerto donde correrá el microservicio de catálogo
        this.webClient = webClientBuilder.baseUrl("http://localhost:8082").build(); 
    }

    @Transactional
    public Pedido changeOrderStatus(Long orderId, OrderStatus newStatus) {
        Pedido order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        OrderStatus currentStatus = order.getStatus();

        if (newStatus == OrderStatus.DISPATCHED && (currentStatus != OrderStatus.ACCEPTED && currentStatus != OrderStatus.PREPARING)) {
            throw new IllegalStateException("Violación de regla: No se puede despachar un pedido que no ha sido aceptado o preparado.");
        }
        if (currentStatus == OrderStatus.DELIVERED || currentStatus == OrderStatus.CANCELLED) {
            throw new IllegalStateException("No se puede cambiar el estado de un pedido finalizado o cancelado.");
        }

        order.setStatus(newStatus);
        Pedido updatedOrder = orderRepository.save(order);

        // 1. Regla de negocio: Descontar stock síncronamente al aceptar
        if (newStatus == OrderStatus.ACCEPTED) {
            webClient.put()
                    .uri("/api/catalog/discount?producto={prod}", updatedOrder.getProducto())
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block(); // Bloqueante por simplicidad del caso
        }

        EventEnvelope<Pedido> envelope = new EventEnvelope<>("Order" + newStatus.name(), updatedOrder);

        // 2. Emitir evento de dominio a Kafka (orders.events)
        kafkaTemplate.send("orders.events", String.valueOf(updatedOrder.getId()), envelope);

        // 3. Enviar comando asíncrono a RabbitMQ para notificar al cliente
        rabbitTemplate.convertAndSend("cmd.direct", "email.send", envelope);
        
        return updatedOrder;
    }
}