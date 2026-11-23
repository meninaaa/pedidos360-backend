package com.pedidos360.audit_service.listener;

import com.pedidos360.audit_service.dto.OrderEventDTO;
import com.pedidos360.audit_service.service.AuditConsumerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

@Component
public class AuditEventListener {

    private static final Logger log = LoggerFactory.getLogger(AuditEventListener.class);
    private final AuditConsumerService auditConsumerService;

    public AuditEventListener(AuditConsumerService auditConsumerService) {
        this.auditConsumerService = auditConsumerService;
    }

    @RetryableTopic(
            attempts = "3",
            backoff = @Backoff(delay = 1000, multiplier = 2.0),
            dltTopicSuffix = ".DLT"
    )
    @KafkaListener(topics = "audit.timeline", groupId = "audit-group")
    public void onOrderEvent(
            OrderEventDTO event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {
        log.info("Evento recibido en audit.timeline [partition={}, offset={}, orderId={}]",
                partition, offset, event != null ? event.getId() : null);
        auditConsumerService.registrarEventoAuditoria(event);
    }

    @DltHandler
    public void onDltEvent(
            OrderEventDTO event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(value = KafkaHeaders.EXCEPTION_MESSAGE, required = false) String error) {
        log.error("Mensaje enviado a DLT [topic={}, orderId={}, error={}]",
                topic, event != null ? event.getId() : "desconocido", error);
    }
}