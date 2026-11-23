package com.pedidos360.audit_service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pedidos360.audit_service.dto.OrderEventDTO;
import com.pedidos360.audit_service.entity.AuditLog;
import com.pedidos360.audit_service.listener.AuditEventListener;
import com.pedidos360.audit_service.repository.AuditLogRepository;
import com.pedidos360.audit_service.service.AuditConsumerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuditEventListenerTest {

    private AuditLogRepository repository;
    private AuditEventListener listener;

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(AuditLogRepository.class);
        when(repository.save(any(AuditLog.class))).thenAnswer(invocation -> invocation.getArgument(0));
        AuditConsumerService service = new AuditConsumerService(repository, new ObjectMapper().findAndRegisterModules());
        listener = new AuditEventListener(service);
    }

    @Test
    void cuandoEventoAuditoriaValido_entoncesPersisteEnBaseDeDatos() {
        OrderEventDTO event = new OrderEventDTO();
        event.setId(50L);
        event.setCustomerId("empresa@cliente.cl");
        event.setStatus("ACEPTADO");
        event.setEventType("ORDER_EVENT");
        event.setActor("admin@pedidos360.cl");
        event.setEventTimestamp(LocalDateTime.now());

        listener.onOrderEvent(event, 0, 12L);

        verify(repository, times(1)).save(any(AuditLog.class));
    }

    @Test
    void cuandoEventoAuditoriaInvalido_entoncesLanzaExcepcionParaReintentoYDlt() {
        OrderEventDTO invalidEvent = new OrderEventDTO();

        assertThrows(IllegalArgumentException.class, () -> listener.onOrderEvent(invalidEvent, 1, 15L));
        verify(repository, never()).save(any());
    }
}