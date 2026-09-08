package com.pedidos360.catalog_service.controller;

import com.pedidos360.catalog_service.entity.Product;
import com.pedidos360.catalog_service.repository.ProductRepository;
import com.pedidos360.catalog_service.service.CatalogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/catalog")
@Tag(name = "Catálogo", description = "CRUD de productos y control de stock")
public class CatalogController {

    private final ProductRepository productRepository;
    private final CatalogService catalogService;

    public CatalogController(ProductRepository productRepository, CatalogService catalogService) {
        this.productRepository = productRepository;
        this.catalogService = catalogService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR')")
    @Operation(summary = "Obtener catálogo", description = "Lista todos los productos.")
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @PutMapping("/discount")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR')")
    @Operation(summary = "Descontar stock", description = "Disminuye el stock en 1 unidad síncronamente.")
    public void discountStock(@RequestParam String producto) {
        catalogService.discountStock(producto);
    }
}