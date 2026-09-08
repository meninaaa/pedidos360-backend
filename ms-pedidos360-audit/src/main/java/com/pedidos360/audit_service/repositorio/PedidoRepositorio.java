package com.pedidos360.audit_service.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pedidos360.audit_service.entity.Pedido;

@Repository
public interface PedidoRepositorio extends JpaRepository<Pedido, Long> {
}