package com.pedidos360.audit_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pedidos360.audit_service.entity.Pedido;
import com.pedidos360.audit_service.repositorio.PedidoRepositorio;

import java.util.List;

@RestController
@RequestMapping("/internos")
public class PedidoController {

    @Autowired
    private PedidoRepositorio repositorio;

    @GetMapping("/pedidos")
    public List<Pedido> obtenerPedidos() {
        // Esto obliga al controlador a sacar los datos directamente de la base de datos H2
        return repositorio.findAll(); 
    }
}