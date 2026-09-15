package com.pedidos360.audit_service.service;

import com.pedidos360.audit_service.entity.AuditLog;
import com.pedidos360.audit_service.repository.AuditLogRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuditConsumerService {

    private final AuditLogRepository repository;

    public AuditConsumerService(AuditLogRepository repository) {
        this.repository = repository;
    }

    // Configura 3 reintentos antes de mandar al DLT (*.DLT)
    @RetryableTopic(
        attempts = "3",
        autoCreateTopics = "true",
        dltTopicSuffix = ".DLT"
    )
    @KafkaListener(topics = "audit.timeline", groupId = "audit-group")
    public void consumeEvent(String message) {
        try {
            System.out.println("📥 [AUDITORIA] Mensaje recibido de Kafka: " + message);

            AuditLog log = new AuditLog();
            log.setEventType("ORDER_EVENT");
            
            // Forzamos un actor por defecto
            log.setActor("SYSTEM"); 
            log.setTimestamp(LocalDateTime.now());

            // Recortamos el string a 3900 caracteres para evitar el error 'Data too long' en Oracle
            String safePayload = message;
            if (message != null && message.length() > 3900) {
                safePayload = message.substring(0, 3900) + "... [recortado]";
            }
            log.setPayload(safePayload);
            
            repository.save(log);
            System.out.println("✅ [AUDITORIA] Guardado en BD exitosamente.");

        } catch (Exception e) {
            System.err.println("❌ [AUDITORIA] Fallo crítico al guardar: " + e.getMessage());
            // Se debe lanzar la excepción para que Kafka active el reintento y eventual envío al DLT
            throw new RuntimeException("Error procesando evento para Auditoría", e);
        }
    }
}