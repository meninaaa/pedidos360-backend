package com.pedidos360.orders_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.pedidos360.orders_service.entity.Order;
import com.pedidos360.orders_service.repository.OrderRepository;
import com.pedidos360.orders_service.service.OrderService;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Pedidos", description = "CRUD de Pedidos y orquestación de estados")
public class OrderController {

    private final OrderRepository orderRepository;
    private final OrderService orderService;

    public OrderController(OrderRepository orderRepository, OrderService orderService) {
        this.orderRepository = orderRepository;
        this.orderService = orderService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('Admin', 'Operador', 'Cliente')")
    @Operation(summary = "Obtener todos los pedidos", description = "Retorna el listado de pedidos. Requiere rol Admin, Operador o Cliente.")
    public List<Order> obtenerPedidos() {
        return orderRepository.findAll(); 
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('Admin', 'Operador')")
    @Operation(summary = "Cambiar estado del pedido", description = "Actualiza el estado, coordina stock y emite eventos/notificaciones.")
    public Order cambiarEstado(@PathVariable Long id, @RequestParam Order.OrderStatus nuevoEstado) {
        return orderService.updateOrderStatus(id, nuevoEstado);
    }
}