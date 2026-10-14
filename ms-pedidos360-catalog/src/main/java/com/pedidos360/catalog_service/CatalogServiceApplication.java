package com.pedidos360.catalog_service;

import com.pedidos360.catalog_service.entity.Product;
import com.pedidos360.catalog_service.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import java.util.Arrays;

@SpringBootApplication
public class CatalogServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CatalogServiceApplication.class, args);
    }

    @Bean
    CommandLineRunner initDatabase(ProductRepository repository) {
        return args -> {
            // Forzamos la recarga limpia con un catálogo robusto y ampliado
            repository.deleteAll();
            repository.saveAll(Arrays.asList(
                new Product("MacBook Pro M3", 10, 2500000.0),
                new Product("Monitor LG UltraWide 34\"", 15, 350000.0),
                new Product("Teclado Mecánico Keychron K2", 50, 100000.0),
                new Product("Mouse Logitech MX Master 3S", 30, 90000.0),
                new Product("Audífonos Sony WH-1000XM5", 25, 280000.0),
                new Product("Hub USB-C Satechi", 40, 65000.0),
                new Product("Silla Ergonómica Herman Miller", 3, 1200000.0), // Stock bajo
                new Product("Escritorio Elevable FlexiSpot", 4, 450000.0),  // Stock bajo
                new Product("Disco Duro Externo SSD 1TB Samsung", 60, 110000.0),
                new Product("Cámara Web Logitech Brio 4K", 20, 150000.0),
                new Product("Tablet iPad Air M2", 12, 850000.0),
                new Product("Impresora Multifuncional EcoTank", 0, 240000.0), // Agotado
                new Product("Microfonó Shure MV7 USB", 18, 220000.0),
                new Product("Docking Station Thunderbolt 4", 7, 310000.0),   // Stock bajo
                new Product("Router Wi-Fi 6 Mesh ASUS", 14, 190000.0)
            ));
            System.out.println("✅ Catálogo inicializado masivamente con 15 productos (incluyendo stock bajo y agotados).");
        };
    }
}