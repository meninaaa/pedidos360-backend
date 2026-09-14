package com.pedidos360.orders_service.repository;

import com.pedidos360.orders_service.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByStatusIn(List<Order.OrderStatus> statuses);
    List<Order> findByCustomerId(String customerId);
}