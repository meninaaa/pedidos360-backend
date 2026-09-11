package com.pedidos360.catalog_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/catalog")
@Tag(name = "Catálogo", description = "Gestión de productos y stock disponible")
public class CatalogController {

    @GetMapping("/products")
    @Operation(summary = "Listar productos", description = "Retorna el catálogo disponible de productos y stock.")
    public ResponseEntity<?> getProducts() {
        // Datos simulados de prueba para cumplir con el despliegue rápido del encargo
        Map<String, Object> p1 = new HashMap<>();
        p1.put("id", 1);
        p1.put("name", "MacBook Pro M3");
        p1.put("stock", 10);
        p1.put("price", 1500000);

        Map<String, Object> p2 = new HashMap<>();
        p2.put("id", 2);
        p2.put("name", "Monitor LG UltraWide 34");
        p2.put("stock", 5);
        p2.put("price", 350000);

        return ResponseEntity.ok(List.of(p1, p2));
    }
}