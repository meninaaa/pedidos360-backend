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
            if (repository.count() == 0) {
                repository.saveAll(Arrays.asList(
                    new Product("MacBook Pro M3", 10, 2500000.0),
                    new Product("Monitor LG UltraWide 34\"", 15, 350000.0),
                    new Product("Teclado Mecánico Keychron K2", 50, 100000.0),
                    new Product("Mouse Logitech MX Master 3S", 30, 90000.0),
                    new Product("Audífonos Sony WH-1000XM5", 25, 280000.0),
                    new Product("Hub USB-C Satechi", 40, 65000.0),
                    new Product("Silla Ergonómica Herman Miller", 5, 1200000.0),
                    new Product("Escritorio Elevable FlexiSpot", 8, 450000.0),
                    new Product("Disco Duro Externo SSD 1TB Samsung", 60, 110000.0),
                    new Product("Cámara Web Logitech Brio 4K", 20, 150000.0)
                ));
                System.out.println("✅ Catálogo inicializado con 10 productos en Oracle.");
            }
        };
    }
}