package com.pedidos360.audit_service.service;

import com.pedidos360.audit_service.entity.AuditLog;
import com.pedidos360.audit_service.repository.AuditLogRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class AuditKafkaConsumer {

    private final AuditLogRepository auditLogRepository;

    public AuditKafkaConsumer(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @KafkaListener(topics = "orders-topic", groupId = "audit-group")
    public void consumeOrderEvent(String message) {
        System.out.println("Evento de orden recibido en Audit: " + message);
        
        // Asumiendo que el mensaje es un JSON o un String descriptivo
        AuditLog log = new AuditLog("ORDER_EVENT", message);
        auditLogRepository.save(log);
    }
}