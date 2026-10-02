package com.pedidos360.orders_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.pedidos360.orders_service.entity.Order;
import com.pedidos360.orders_service.repository.OrderRepository;
import com.pedidos360.orders_service.service.OrderService;

import java.util.Arrays;
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
    @PreAuthorize("hasRole('Admin')")
    @Operation(summary = "Obtener todos los pedidos", description = "Vista global. Solo Admin.")
    public List<Order> obtenerPedidos() {
        return orderRepository.findAll();
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('Admin', 'Operador')")
    @Operation(summary = "Pedidos en curso y pendientes", description = "Pedidos que aún no llegan a un estado final. Para el Dashboard de Operador.")
    public List<Order> obtenerPedidosPendientes() {
        return orderRepository.findByStatusIn(Arrays.asList(
                Order.OrderStatus.CREADO,
                Order.OrderStatus.ACEPTADO,
                Order.OrderStatus.EN_PREPARACION,
                Order.OrderStatus.DESPACHADO
        ));
    }

    @GetMapping("/customer")
    @PreAuthorize("hasAnyRole('Admin', 'Operador', 'Cliente')")
    @Operation(summary = "Mis pedidos", description = "Pedidos del usuario autenticado por Query Param.")
    public ResponseEntity<List<Order>> getOrdersByCustomer(@RequestParam String email) {
        return ResponseEntity.ok(orderService.getOrdersByCustomer(email));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('Admin', 'Operador')")
    @Operation(summary = "Cambiar estado del pedido", description = "Actualiza el estado, coordina stock y emite eventos/notificaciones.")
    public ResponseEntity<?> cambiarEstado(@PathVariable Long id, @RequestParam Order.OrderStatus nuevoEstado) {
        
        return orderRepository.findById(id).map(order -> {
            String estadoActual = order.getStatus().name();
            
            if (nuevoEstado == Order.OrderStatus.DESPACHADO) {
                if (!estadoActual.equalsIgnoreCase("ACEPTADO") && !estadoActual.equalsIgnoreCase("EN_PREPARACION")) {
                    return ResponseEntity.badRequest().body("Error: No se puede despachar un pedido sin haberlo aceptado previamente.");
                }
            }
            
            Order updatedOrder = orderService.updateOrderStatus(id, nuevoEstado);
            return ResponseEntity.ok(updatedOrder);
            
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('Admin', 'Operador', 'Cliente')")
    @Operation(summary = "Crear pedido", description = "Registra un nuevo pedido y emite eventos a Kafka y notificaciones por RabbitMQ.")
    public ResponseEntity<Order> crearPedido(@RequestBody Order order) {
        Order nuevoPedido = orderService.createOrder(order);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoPedido);
    }
}