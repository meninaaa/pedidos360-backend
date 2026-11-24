package com.pedidos360.orders_service;

import com.pedidos360.orders_service.entity.Order;
import com.pedidos360.orders_service.repository.OrderRepository;
import com.pedidos360.orders_service.service.OrderService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

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
    CommandLineRunner initDatabase(OrderRepository repositorio, OrderService orderService) {
        return args -> {
            if (repositorio.count() > 0) {
                return;
            }

            // Pedido 1: Ciclo completo ENTREGADO
            Order p1 = crearPedido(repositorio, orderService, "Empresa Alpha S.A.", 450000.0, 1L);
            avanzarEstado(repositorio, orderService, p1, Order.OrderStatus.ACEPTADO);
            avanzarEstado(repositorio, orderService, p1, Order.OrderStatus.EN_PREPARACION);
            avanzarEstado(repositorio, orderService, p1, Order.OrderStatus.DESPACHADO);
            avanzarEstado(repositorio, orderService, p1, Order.OrderStatus.ENTREGADO);

            // Pedido 2: DESPACHADO
            Order p2 = crearPedido(repositorio, orderService, "Retail Sur Ltda.", 1200000.0, 7L);
            avanzarEstado(repositorio, orderService, p2, Order.OrderStatus.ACEPTADO);
            avanzarEstado(repositorio, orderService, p2, Order.OrderStatus.EN_PREPARACION);
            avanzarEstado(repositorio, orderService, p2, Order.OrderStatus.DESPACHADO);

            // Pedido 3: EN_PREPARACION
            Order p3 = crearPedido(repositorio, orderService, "Juan Pérez", 35000.0, 3L);
            avanzarEstado(repositorio, orderService, p3, Order.OrderStatus.ACEPTADO);
            avanzarEstado(repositorio, orderService, p3, Order.OrderStatus.EN_PREPARACION);

            // Pedido 4: CREADO
            crearPedido(repositorio, orderService, "Empresa Alpha S.A.", 89000.0, 4L);

            // Pedido 5: ACEPTADO
            Order p5 = crearPedido(repositorio, orderService, "María Gómez", 150000.0, 10L);
            avanzarEstado(repositorio, orderService, p5, Order.OrderStatus.ACEPTADO);

            // Pedido 6: CANCELADO
            Order p6 = crearPedido(repositorio, orderService, "Tech Solutions", 540000.0, 2L);
            avanzarEstado(repositorio, orderService, p6, Order.OrderStatus.CANCELADO);

            // Pedido 7: CREADO
            crearPedido(repositorio, orderService, "Retail Sur Ltda.", 230000.0, 5L);

            // Pedido 8: DESPACHADO
            Order p8 = crearPedido(repositorio, orderService, "Juan Pérez", 12500.0, 6L);
            avanzarEstado(repositorio, orderService, p8, Order.OrderStatus.ACEPTADO);
            avanzarEstado(repositorio, orderService, p8, Order.OrderStatus.EN_PREPARACION);
            avanzarEstado(repositorio, orderService, p8, Order.OrderStatus.DESPACHADO);

            // Pedido 9: ENTREGADO
            Order p9 = crearPedido(repositorio, orderService, "Logística Austral SpA", 850000.0, 11L);
            avanzarEstado(repositorio, orderService, p9, Order.OrderStatus.ACEPTADO);
            avanzarEstado(repositorio, orderService, p9, Order.OrderStatus.ENTREGADO);

            // Pedido 10: EN_PREPARACION
            Order p10 = crearPedido(repositorio, orderService, "Inversiones Andes", 310000.0, 14L);
            avanzarEstado(repositorio, orderService, p10, Order.OrderStatus.ACEPTADO);
            avanzarEstado(repositorio, orderService, p10, Order.OrderStatus.EN_PREPARACION);

            // Pedido 11: CREADO
            crearPedido(repositorio, orderService, "Comercial Bío Bío", 95000.0, 9L);

            // Pedido 12: CANCELADO
            Order p12 = crearPedido(repositorio, orderService, "Distribuidora del Norte", 2100000.0, 1L);
            avanzarEstado(repositorio, orderService, p12, Order.OrderStatus.CANCELADO);

            System.out.println("Base de datos inicializada con 12 pedidos y timeline completo en Kafka.");
        };
    }

    private Order crearPedido(OrderRepository repositorio, OrderService orderService, String customerId, double total, Long productId) {
        Order order = new Order();
        order.setCustomerId(customerId);
        order.setTotal(total);
        order.setProductId(productId);
        order.setStatus(Order.OrderStatus.CREADO);
        repositorio.save(order);
        publicarConReintento(orderService, order);
        return order;
    }

    private void avanzarEstado(OrderRepository repositorio, OrderService orderService, Order order, Order.OrderStatus nuevoEstado) {
        order.setStatus(nuevoEstado);
        repositorio.save(order);
        publicarConReintento(orderService, order);
    }

    private void publicarConReintento(OrderService orderService, Order order) {
        for (int intento = 1; intento <= 8; intento++) {
            try {
                orderService.publicarEventoKafkaSincrono(order);
                return;
            } catch (Exception e) {
                if (intento == 8) {
                    System.err.println("[ERROR] No se pudo emitir evento semilla para pedido " + order.getId() + ": " + e.getMessage());
                    return;
                }
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }
}