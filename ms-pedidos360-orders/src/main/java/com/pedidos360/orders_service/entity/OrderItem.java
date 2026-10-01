package com.pedidos360.orders_service.entity;

import jakarta.persistence.Embeddable;

@Embeddable
public class OrderItem {

    private Long productId;
    private Integer quantity;

    public OrderItem() {}

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}