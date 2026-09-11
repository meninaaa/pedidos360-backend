package com.pedidos360.report_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Object, Long> {
    
    // Consulta nativa simulada. Ajusta el nombre de la tabla ('orders') 
    // y la columna ('status') según tu diseño en ms-orders.
    @Query(value = "SELECT status, COUNT(*) as total FROM orders GROUP BY status", nativeQuery = true)
    List<Object[]> getOrdersSummaryByStatus();
}