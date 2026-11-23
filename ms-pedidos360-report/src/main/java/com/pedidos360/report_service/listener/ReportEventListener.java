package com.pedidos360.report_service.listener;

import com.pedidos360.report_service.dto.OrderEventDTO;
import com.pedidos360.report_service.service.ReportMetricsService;
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
public class ReportEventListener {

    private static final Logger log = LoggerFactory.getLogger(ReportEventListener.class);
    private final ReportMetricsService reportMetricsService;

    public ReportEventListener(ReportMetricsService reportMetricsService) {
        this.reportMetricsService = reportMetricsService;
    }

    @RetryableTopic(
            attempts = "3",
            backoff = @Backoff(delay = 1000, multiplier = 2.0),
            dltTopicSuffix = ".DLT"
    )
    @KafkaListener(topics = "orders.events", groupId = "report-group")
    public void onOrderEvent(
            OrderEventDTO event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {
        log.info("Evento recibido en orders.events [partition={}, offset={}, orderId={}, status={}]",
                partition, offset, event != null ? event.getId() : null, event != null ? event.getStatus() : null);
        reportMetricsService.procesarEventoPedido(event);
    }

    @DltHandler
    public void onDltEvent(
            OrderEventDTO event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(value = KafkaHeaders.EXCEPTION_MESSAGE, required = false) String error) {
        log.error("Evento de reporte enviado a DLT [topic={}, orderId={}, error={}]",
                topic, event != null ? event.getId() : "desconocido", error);
    }
}