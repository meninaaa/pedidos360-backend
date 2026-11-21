package com.pedidos360.audit_service.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pedidos360.audit_service.entity.AuditLog;
import com.pedidos360.audit_service.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

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
        this(repository, new ObjectMapper());
    }

    // Configura 3 reintentos antes de mandar al DLT (audit.timeline.DLT)
    @RetryableTopic(
        attempts = "3",
        autoCreateTopics = "true",
        dltTopicSuffix = ".DLT"
    )
    @KafkaListener(topics = "audit.timeline", groupId = "audit-group")
    public void consumeEvent(String message) {
        try {
            System.out.println("📥 [AUDITORIA] Mensaje recibido de Kafka: " + message);

            if (message == null || message.isBlank()) {
                throw new IllegalArgumentException("El mensaje recibido en audit.timeline está vacío");
            }

            // Valida que el payload sea un objeto JSON válido (si está corrupto, activa reintentos y DLT)
            JsonNode jsonNode = objectMapper.readTree(message);
            if (!jsonNode.isObject()) {
                throw new IllegalArgumentException("El payload recibido no es un objeto JSON válido");
            }

            String eventType = jsonNode.hasNonNull("eventType")
                    ? jsonNode.get("eventType").asText()
                    : "ORDER_EVENT";

            String actor = jsonNode.hasNonNull("actor")
                    ? jsonNode.get("actor").asText()
                    : (jsonNode.hasNonNull("userEmail") ? jsonNode.get("userEmail").asText() : "SYSTEM");

            AuditLog log = new AuditLog();
            log.setEventType(eventType);
            log.setActor(actor);
            log.setTimestamp(LocalDateTime.now());

            // Recortamos el string a 3900 caracteres para evitar el error 'Data too long' en Oracle
            String safePayload = message;
            if (message.length() > 3900) {
                safePayload = message.substring(0, 3900) + "... [recortado]";
            }
            log.setPayload(safePayload);

            repository.save(log);
            System.out.println("✅ [AUDITORIA] Guardado en BD exitosamente (" + eventType + ").");

        } catch (Exception e) {
            System.err.println("❌ [AUDITORIA] Fallo crítico al procesar/guardar: " + e.getMessage());
            // Relanza la excepción para que Kafka ejecute los 3 reintentos y envíe al DLT
            throw new RuntimeException("Error procesando evento para Auditoría", e);
        }
    }

    @DltHandler
    public void handleDlt(String message, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        System.err.println("🚨 [AUDITORIA-DLT] Mensaje enviado a " + topic + ": " + message);
    }
}