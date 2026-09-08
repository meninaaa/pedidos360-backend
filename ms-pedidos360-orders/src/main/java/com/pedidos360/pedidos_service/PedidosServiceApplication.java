package com.pedidos360.pedidos_service;

import com.pedidos360.pedidos_service.entity.Pedido;
import com.pedidos360.pedidos_service.model.OrderStatus;
import com.pedidos360.pedidos_service.repositorio.PedidoRepositorio;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import java.util.Arrays;

@SpringBootApplication
public class PedidosServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PedidosServiceApplication.class, args);
    }

    @Bean
    CommandLineRunner initDatabase(PedidoRepositorio repositorio) {
        return args -> {
            if (repositorio.count() == 0) {
                Pedido p1 = new Pedido(); p1.setProducto("MacBook Pro M3"); p1.setStatus(OrderStatus.DELIVERED);
                Pedido p2 = new Pedido(); p2.setProducto("Monitor LG UltraWide 34\""); p2.setStatus(OrderStatus.DISPATCHED);
                Pedido p3 = new Pedido(); p3.setProducto("Teclado Mecánico Keychron K2"); p3.setStatus(OrderStatus.PREPARING);
                Pedido p4 = new Pedido(); p4.setProducto("Mouse Logitech MX Master 3S"); p4.setStatus(OrderStatus.CREATED);
                
                repositorio.saveAll(Arrays.asList(p1, p2, p3, p4));
                System.out.println("✅ Base de datos inicializada con el catálogo de prueba!");
            }
        };
    }
}