package com.pedidos360.report_service;

import com.pedidos360.report_service.dto.OrderEventDTO;
import com.pedidos360.report_service.dto.OrderItemDTO;
import com.pedidos360.report_service.entity.OrderEventLog;
import com.pedidos360.report_service.listener.ReportEventListener;
import com.pedidos360.report_service.repository.OrderEventLogRepository;
import com.pedidos360.report_service.service.ReportMetricsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReportEventListenerTest {

    private OrderEventLogRepository repository;
    private ReportMetricsService metricsService;
    private ReportEventListener listener;

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(OrderEventLogRepository.class);
        when(repository.save(any(OrderEventLog.class))).thenAnswer(invocation -> invocation.getArgument(0));
        metricsService = new ReportMetricsService(repository);
        listener = new ReportEventListener(metricsService);
    }

    @Test
    void cuandoFlujoPedidoCompletado_entoncesActualizaMetricasYTopProductos() {
        LocalDateTime inicio = LocalDateTime.now().minusHours(2);

        OrderEventDTO creado = new OrderEventDTO();
        creado.setId(300L);
        creado.setCustomerId("cliente@pedidos360.cl");
        creado.setStatus("CREADO");
        creado.setTotal(120000.0);
        creado.setEventTimestamp(inicio);

        listener.onOrderEvent(creado, 0, 1L);
        assertEquals(1, metricsService.getMetrics().get("pedidosActivos"));

        OrderEventDTO entregado = new OrderEventDTO();
        entregado.setId(300L);
        entregado.setCustomerId("cliente@pedidos360.cl");
        entregado.setStatus("ENTREGADO");
        entregado.setTotal(120000.0);
        entregado.setEventTimestamp(LocalDateTime.now());
        entregado.setItems(List.of(new OrderItemDTO(7L, "Monitor Gamer 27", 2, 60000.0)));

        listener.onOrderEvent(entregado, 0, 2L);

        Map<String, Object> kpis = metricsService.getMetrics();
        assertEquals(0, kpis.get("pedidosActivos"));
        assertEquals(120000.0, (double) kpis.get("ventasTotales"), 0.01);

        List<java.util.Map<String, Object>> top = metricsService.getTopProductos();
        assertEquals("Monitor Gamer 27", top.get(0).get("nombre"));
        assertEquals(2, top.get(0).get("cantidad"));
        verify(repository, times(2)).save(any(OrderEventLog.class));
    }

    @Test
    void cuandoEventoReporteInvalido_entoncesLanzaExcepcionParaReintentoYDlt() {
        OrderEventDTO invalidEvent = new OrderEventDTO();

        assertThrows(IllegalArgumentException.class, () -> listener.onOrderEvent(invalidEvent, 2, 9L));
        verify(repository, never()).save(any());
    }
}