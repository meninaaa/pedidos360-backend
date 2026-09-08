package com.pedidos360.audit_service;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.pedidos360.audit_service.entity.Pedido;
import com.pedidos360.audit_service.repositorio.PedidoRepositorio;

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
                Pedido p1 = new Pedido(); p1.setProducto("MacBook Pro M3"); p1.setEstado("Entregado");
                Pedido p2 = new Pedido(); p2.setProducto("Monitor LG UltraWide 34\""); p2.setEstado("En tránsito");
                Pedido p3 = new Pedido(); p3.setProducto("Teclado Mecánico Keychron K2"); p3.setEstado("En preparación");
                Pedido p4 = new Pedido(); p4.setProducto("Mouse Logitech MX Master 3S"); p4.setEstado("Pendiente de pago");
                Pedido p5 = new Pedido(); p5.setProducto("Silla Ergonómica Herman Miller"); p5.setEstado("Cancelado");
                Pedido p6 = new Pedido(); p6.setProducto("Audífonos Sony WH-1000XM5"); p6.setEstado("En tránsito");
                Pedido p7 = new Pedido(); p7.setProducto("Escritorio Elevable FlexiSpot"); p7.setEstado("En preparación");
                
                // Guardado masivo siguiendo buenas prácticas
                repositorio.saveAll(Arrays.asList(p1, p2, p3, p4, p5, p6, p7));
                
                System.out.println("✅ Base de datos H2 inicializada y poblada con catálogo completo!");
            }
        };
    }
}