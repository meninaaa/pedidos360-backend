package com.pedidos360.audit_service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pedidos360.audit_service.dto.OrderEventDTO;
import com.pedidos360.audit_service.entity.AuditLog;
import com.pedidos360.audit_service.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuditConsumerService {

    private final AuditLogRepository repository;
    private final ObjectMapper objectMapper;

    @Autowired
    public AuditConsumerService(AuditLogRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    public AuditConsumerService(AuditLogRepository repository) {
        this(repository, new ObjectMapper().findAndRegisterModules());
    }

    @Transactional
    public AuditLog registrarEventoAuditoria(OrderEventDTO event) {
        if (event == null || event.getId() == null || event.getStatus() == null) {
            throw new IllegalArgumentException("Payload de auditoría inválido: id y status son obligatorios");
        }

        try {
            String rawPayload = objectMapper.writeValueAsString(event);
            if (rawPayload.length() > 3900) {
                rawPayload = rawPayload.substring(0, 3900) + "... [recortado]";
            }

            AuditLog log = new AuditLog();
            log.setEventType(event.getEventType() != null ? event.getEventType() : "ORDER_EVENT");
            log.setActor(event.getActor() != null ? event.getActor() : "SYSTEM");
            log.setTimestamp(event.getEventTimestamp() != null ? event.getEventTimestamp() : LocalDateTime.now());
            log.setPayload(rawPayload);

            return repository.save(log);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Error serializando evento para Auditoría", e);
        }
    }
}