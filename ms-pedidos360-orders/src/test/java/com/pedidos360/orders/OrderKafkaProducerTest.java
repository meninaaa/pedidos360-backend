package com.pedidos360.orders;

import com.pedidos360.orders_service.dto.OrderEventDTO;
import com.pedidos360.orders_service.entity.Order;
import com.pedidos360.orders_service.repository.OrderRepository;
import com.pedidos360.orders_service.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderKafkaProducerTest {

    private OrderRepository orderRepository;
    private RabbitTemplate rabbitTemplate;
    private KafkaTemplate<String, OrderEventDTO> kafkaTemplate;
    private RestTemplate restTemplate;
    private OrderService orderService;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        orderRepository = Mockito.mock(OrderRepository.class);
        rabbitTemplate = Mockito.mock(RabbitTemplate.class);
        kafkaTemplate = Mockito.mock(KafkaTemplate.class);
        restTemplate = Mockito.mock(RestTemplate.class);
        orderService = new OrderService(orderRepository, rabbitTemplate, kafkaTemplate, restTemplate);
    }

    @Test
    void cuandoCrearPedido_entoncesPublicaDtoEnAmbosTopicosKafka() {
        Order input = new Order();
        input.setCustomerId("cliente@pedidos360.cl");
        input.setProductId(10L);
        input.setTotal(45000.0);

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order saved = invocation.getArgument(0);
            saved.setId(101L);
            return saved;
        });

        orderService.createOrder(input);

        ArgumentCaptor<OrderEventDTO> eventCaptor = ArgumentCaptor.forClass(OrderEventDTO.class);
        verify(kafkaTemplate, times(1)).send(eq("orders.events"), eq("101"), eventCaptor.capture());
        verify(kafkaTemplate, times(1)).send(eq("audit.timeline"), eq("101"), any(OrderEventDTO.class));

        OrderEventDTO published = eventCaptor.getValue();
        assertEquals(101L, published.getId());
        assertEquals("CREADO", published.getStatus());
        assertEquals("ORDER_EVENT", published.getEventType());
        assertNotNull(published.getEventTimestamp());
    }

    @Test
    void cuandoActualizarEstadoAEntregado_entoncesEmiteEventoConNuevoEstado() {
        Order existing = new Order();
        existing.setId(202L);
        existing.setCustomerId("cliente@pedidos360.cl");
        existing.setStatus(Order.OrderStatus.DESPACHADO);
        existing.setTotal(80000.0);

        when(orderRepository.findById(202L)).thenReturn(Optional.of(existing));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        orderService.updateOrderStatus(202L, Order.OrderStatus.ENTREGADO);

        ArgumentCaptor<OrderEventDTO> eventCaptor = ArgumentCaptor.forClass(OrderEventDTO.class);
        verify(kafkaTemplate, times(1)).send(eq("orders.events"), eq("202"), eventCaptor.capture());
        verify(kafkaTemplate, times(1)).send(eq("audit.timeline"), eq("202"), any(OrderEventDTO.class));

        assertEquals("ENTREGADO", eventCaptor.getValue().getStatus());
    }
}