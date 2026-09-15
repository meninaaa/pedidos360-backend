package com.pedidos360.catalog_service.controller;

import com.pedidos360.catalog_service.entity.Product;
import com.pedidos360.catalog_service.repository.ProductRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/catalog")
@Tag(name = "Catálogo", description = "Gestión de productos y stock disponible")
public class CatalogController {

    private final ProductRepository productRepository;

    public CatalogController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @GetMapping("/products")
    @Operation(summary = "Listar productos", description = "Retorna el catálogo disponible de productos y stock.")
    public ResponseEntity<List<Product>> getProducts() {
        List<Product> products = productRepository.findAll();
        return ResponseEntity.ok(products);
    }

    @PostMapping("/products")
    @Operation(summary = "Crear producto", description = "Añade un nuevo producto a la base de datos.")
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        Product savedProduct = productRepository.save(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedProduct);
    }

    @PutMapping("/products/{id}")
    @Operation(summary = "Actualizar producto", description = "Modifica los datos de un producto existente.")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product productDetails) {
        return productRepository.findById(id)
            .map(existingProduct -> {
                existingProduct.setNombre(productDetails.getNombre());
                existingProduct.setPrecio(productDetails.getPrecio());
                existingProduct.setStock(productDetails.getStock());
                
                Product updatedProduct = productRepository.save(existingProduct);
                return ResponseEntity.ok(updatedProduct);
            })
            .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/products/{id}")
    @Operation(summary = "Eliminar producto", description = "Elimina un producto del catálogo por su ID.")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        if (productRepository.existsById(id)) {
            productRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}