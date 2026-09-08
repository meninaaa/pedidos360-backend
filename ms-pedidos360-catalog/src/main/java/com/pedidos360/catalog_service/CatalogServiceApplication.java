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
                    new Product("MacBook Pro M3", 10, 2500.0),
                    new Product("Monitor LG UltraWide 34\"", 15, 600.0),
                    new Product("Teclado Mecánico Keychron K2", 50, 100.0),
                    new Product("Mouse Logitech MX Master 3S", 30, 90.0)
                ));
                System.out.println("✅ Catálogo inicializado exitosamente en Oracle.");
            }
        };
    }
}