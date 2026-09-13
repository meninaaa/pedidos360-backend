package com.pedidos360.orders_service;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.pedidos360.orders_service.entity.Order;
import com.pedidos360.orders_service.repository.OrderRepository;

import java.util.Arrays;

@SpringBootApplication
public class OrdersServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrdersServiceApplication.class, args);
    }

    @Bean
    CommandLineRunner initDatabase(OrderRepository repositorio) {
        return args -> {
            // FORZAR BORRADO: Limpiamos la tabla para que siempre se inyecten estos 8 datos en desarrollo
            repositorio.deleteAll(); 
            
            Order p1 = new Order(); p1.setCustomerId("USR-001"); p1.setStatus(Order.OrderStatus.ENTREGADO);
            Order p2 = new Order(); p2.setCustomerId("USR-002"); p2.setStatus(Order.OrderStatus.DESPACHADO);
            Order p3 = new Order(); p3.setCustomerId("USR-003"); p3.setStatus(Order.OrderStatus.EN_PREPARACION);
            Order p4 = new Order(); p4.setCustomerId("USR-001"); p4.setStatus(Order.OrderStatus.CREADO);
            Order p5 = new Order(); p5.setCustomerId("USR-004"); p5.setStatus(Order.OrderStatus.ACEPTADO);
            Order p6 = new Order(); p6.setCustomerId("USR-005"); p6.setStatus(Order.OrderStatus.CANCELADO);
            Order p7 = new Order(); p7.setCustomerId("USR-002"); p7.setStatus(Order.OrderStatus.CREADO);
            Order p8 = new Order(); p8.setCustomerId("USR-003"); p8.setStatus(Order.OrderStatus.DESPACHADO);
            
            repositorio.saveAll(Arrays.asList(p1, p2, p3, p4, p5, p6, p7, p8));
            System.out.println("✅ Base de datos FORZADA e inicializada con 8 pedidos de prueba variados!");
        };
    }
}