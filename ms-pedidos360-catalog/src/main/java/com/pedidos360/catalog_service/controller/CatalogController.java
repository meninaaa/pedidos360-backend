package com.pedidos360.catalog_service.controller;

import com.pedidos360.catalog_service.entity.Product;
import com.pedidos360.catalog_service.repository.ProductRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/catalog")
@Tag(name = "Catálogo", description = "Gestión de productos y stock disponible")
public class CatalogController {

    private final ProductRepository productRepository;

    // Inyección de dependencias por constructor
    public CatalogController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @GetMapping("/products")
    @Operation(summary = "Listar productos", description = "Retorna el catálogo disponible de productos y stock.")
    public ResponseEntity<List<Product>> getProducts() {
        // Elimina la simulación y extrae los datos reales de la base de datos Oracle
        List<Product> products = productRepository.findAll();
        return ResponseEntity.ok(products);
    }
}