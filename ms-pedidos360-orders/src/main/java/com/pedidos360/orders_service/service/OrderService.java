package com.pedidos360.orders_service.service;

import com.pedidos360.orders_service.entity.Order;
import com.pedidos360.orders_service.repository.OrderRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final RabbitTemplate rabbitTemplate;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public OrderService(OrderRepository orderRepository, RabbitTemplate rabbitTemplate, KafkaTemplate<String, String> kafkaTemplate) {
        this.orderRepository = orderRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.kafkaTemplate = kafkaTemplate;
    }

    public Order createOrder(Order order) {
        order.setStatus(Order.OrderStatus.CREADO);
        Order savedOrder = orderRepository.save(order);

        // 1. Enviar evento a Kafka (orders-topic / audit.timeline)
        String eventPayload = "{\"eventId\": \"" + savedOrder.getId() + "\", \"type\": \"OrderCreated\", \"customerId\": \"" + savedOrder.getCustomerId() + "\"}";
        kafkaTemplate.send("orders-topic", eventPayload);
        kafkaTemplate.send("audit.timeline", eventPayload);

        // 2. Enviar comando de notificación a RabbitMQ
        Map<String, Object> command = new HashMap<>();
        command.put("type", "EMAIL_NOTIFICATION");
        command.put("message", "Su pedido #" + savedOrder.getId() + " ha sido creado exitosamente.");
        rabbitTemplate.convertAndSend("cmd.direct", "email.send", command);

        return savedOrder;
    }

    public Order updateOrderStatus(Long orderId, Order.OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        // Regla clave del caso: No se puede despachar sin aceptar (o estar en preparación)
        if (newStatus == Order.OrderStatus.DESPACHADO && 
            order.getStatus() != Order.OrderStatus.ACEPTADO && 
            order.getStatus() != Order.OrderStatus.EN_PREPARACION) {
            throw new IllegalStateException("Error de negocio: No se puede despachar un pedido que no ha sido aceptado.");
        }

        order.setStatus(newStatus);
        Order updatedOrder = orderRepository.save(order);

        // Emitir evento del cambio de estado a Kafka y Kafka Audit
        String eventPayload = "{\"eventId\": \"" + updatedOrder.getId() + "\", \"type\": \"Order" + newStatus + "\"}";
        kafkaTemplate.send("orders-topic", eventPayload);
        kafkaTemplate.send("audit.timeline", eventPayload);

        // Si pasa a ACEPTADO o DESPACHADO, notificamos vía RabbitMQ
        if (newStatus == Order.OrderStatus.ACEPTADO || newStatus == Order.OrderStatus.DESPACHADO) {
            Map<String, Object> command = new HashMap<>();
            command.put("type", "EMAIL_NOTIFICATION");
            command.put("message", "El estado de su pedido #" + updatedOrder.getId() + " cambió a: " + newStatus);
            rabbitTemplate.convertAndSend("cmd.direct", "email.send", command);
        }

        return updatedOrder;
    }
}