package com.pedidos360.report_service.repository;

import com.pedidos360.report_service.entity.OrderEventLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderEventLogRepository extends JpaRepository<OrderEventLog, Long> {
    List<OrderEventLog> findAllByOrderByEventTimestampAsc();
}