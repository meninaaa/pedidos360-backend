package com.pedidos360.pedidos_service.controller;

import com.pedidos360.pedidos_service.entity.Pedido;
import com.pedidos360.pedidos_service.repositorio.PedidoRepositorio;
import com.pedidos360.pedidos_service.model.OrderStatus;
import com.pedidos360.pedidos_service.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Pedidos", description = "CRUD de Pedidos y orquestación de estados")
public class PedidoController {

    @Autowired
    private PedidoRepositorio repositorio;

    @Autowired
    private OrderService orderService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR', 'CLIENTE')")
    @Operation(summary = "Obtener todos los pedidos", description = "Retorna el listado de pedidos. Requiere rol ADMIN, OPERADOR o CLIENTE.")
    public List<Pedido> obtenerPedidos() {
        return repositorio.findAll(); 
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR')")
    @Operation(summary = "Cambiar estado del pedido", description = "Actualiza el estado, coordina stock y emite eventos/notificaciones.")
    public Pedido cambiarEstado(@PathVariable Long id, @RequestParam OrderStatus nuevoEstado) {
        return orderService.changeOrderStatus(id, nuevoEstado);
    }
}