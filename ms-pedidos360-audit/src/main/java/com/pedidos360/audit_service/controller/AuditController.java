package com.pedidos360.audit_service.controller;

import com.pedidos360.audit_service.entity.AuditLog;
import com.pedidos360.audit_service.repository.AuditLogRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/audit")
@Tag(name = "Auditoría", description = "Timeline de eventos del sistema (Solo lectura)")
public class AuditController {

    private final AuditLogRepository auditLogRepository;

    public AuditController(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('Admin', 'Operador')")
    @Operation(summary = "Obtener timeline de auditoría", description = "Retorna todos los registros de auditoría almacenados mediante Kafka.")
    public List<AuditLog> obtenerLogsAuditoria() {
        return auditLogRepository.findAll();
    }
}