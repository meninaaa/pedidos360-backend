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

            Order p1 = crearPedido(repositorio, kafkaTemplate, objectMapper, "Empresa Alpha S.A.", 450000.0);
            avanzarEstado(repositorio, kafkaTemplate, objectMapper, p1, Order.OrderStatus.ACEPTADO);
            avanzarEstado(repositorio, kafkaTemplate, objectMapper, p1, Order.OrderStatus.EN_PREPARACION);
            avanzarEstado(repositorio, kafkaTemplate, objectMapper, p1, Order.OrderStatus.DESPACHADO);
            avanzarEstado(repositorio, kafkaTemplate, objectMapper, p1, Order.OrderStatus.ENTREGADO);

            Order p2 = crearPedido(repositorio, kafkaTemplate, objectMapper, "Retail Sur Ltda.", 1200000.0);
            avanzarEstado(repositorio, kafkaTemplate, objectMapper, p2, Order.OrderStatus.ACEPTADO);
            avanzarEstado(repositorio, kafkaTemplate, objectMapper, p2, Order.OrderStatus.EN_PREPARACION);
            avanzarEstado(repositorio, kafkaTemplate, objectMapper, p2, Order.OrderStatus.DESPACHADO);

            Order p3 = crearPedido(repositorio, kafkaTemplate, objectMapper, "Juan Pérez", 35000.0);
            avanzarEstado(repositorio, kafkaTemplate, objectMapper, p3, Order.OrderStatus.ACEPTADO);
            avanzarEstado(repositorio, kafkaTemplate, objectMapper, p3, Order.OrderStatus.EN_PREPARACION);

            Order p4 = crearPedido(repositorio, kafkaTemplate, objectMapper, "Empresa Alpha S.A.", 89000.0);

            Order p5 = crearPedido(repositorio, kafkaTemplate, objectMapper, "María Gómez", 150000.0);
            avanzarEstado(repositorio, kafkaTemplate, objectMapper, p5, Order.OrderStatus.ACEPTADO);

            Order p6 = crearPedido(repositorio, kafkaTemplate, objectMapper, "Tech Solutions", 540000.0);
            avanzarEstado(repositorio, kafkaTemplate, objectMapper, p6, Order.OrderStatus.CANCELADO);

            Order p7 = crearPedido(repositorio, kafkaTemplate, objectMapper, "Retail Sur Ltda.", 230000.0);

            Order p8 = crearPedido(repositorio, kafkaTemplate, objectMapper, "Juan Pérez", 12500.0);
            avanzarEstado(repositorio, kafkaTemplate, objectMapper, p8, Order.OrderStatus.ACEPTADO);
            avanzarEstado(repositorio, kafkaTemplate, objectMapper, p8, Order.OrderStatus.EN_PREPARACION);
            avanzarEstado(repositorio, kafkaTemplate, objectMapper, p8, Order.OrderStatus.DESPACHADO);

            System.out.println("Base de datos inicializada con 8 pedidos y timeline completo publicado en orders.events");
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
            // ENVIAMOS A AMBOS TÓPICOS PARA QUE LA AUDITORÍA LOS RECIBA AL ARRANCAR
            kafkaTemplate.send("orders.events", order.getId().toString(), json);
            kafkaTemplate.send("audit.timeline", order.getId().toString(), json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}