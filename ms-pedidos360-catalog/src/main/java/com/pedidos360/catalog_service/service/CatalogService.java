package com.pedidos360.catalog_service.service;

import com.pedidos360.catalog_service.entity.Product;
import com.pedidos360.catalog_service.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CatalogService {

    private final ProductRepository productRepository;

    public CatalogService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional
    public void discountStock(String nombreProducto) {
        Product product = productRepository.findByNombre(nombreProducto)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado en catálogo: " + nombreProducto));

        if (product.getStock() <= 0) {
            throw new IllegalStateException("Sin stock disponible para el producto: " + nombreProducto);
        }

        product.setStock(product.getStock() - 1);
        productRepository.save(product);
    }
}