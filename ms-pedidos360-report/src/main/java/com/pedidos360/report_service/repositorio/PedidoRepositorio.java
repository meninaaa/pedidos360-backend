package com.pedidos360.report_service.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pedidos360.report_service.entity.Pedido;

@Repository
public interface PedidoRepositorio extends JpaRepository<Pedido, Long> {
}