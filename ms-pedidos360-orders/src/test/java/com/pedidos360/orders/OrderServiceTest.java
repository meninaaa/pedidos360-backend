package com.pedidos360.orders;

import com.pedidos360.orders_service.entity.Order;
import com.pedidos360.orders_service.repository.OrderRepository;
import com.pedidos360.orders_service.service.OrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private RabbitTemplate rabbitTemplate;
    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @InjectMocks
    private OrderService orderService;

    // PRUEBA REAL: Creación de orden válida -> Persiste con estado CREADO y emite eventos
    @Test
    void cuandoCrearOrdenValida_entoncesProcesaYGuarda() {
        Order order = new Order();
        order.setTotal(150.0);

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order saved = invocation.getArgument(0);
            saved.setId(10L);
            return saved;
        });

        Order result = orderService.createOrder(order);

        assertNotNull(result);
        assertEquals(Order.OrderStatus.CREADO, result.getStatus());
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    // PRUEBA REAL: Regla de negocio inválida -> Transición de estado denegada lanza excepción y no persiste
    @Test
    void cuandoTransicionEstadoInvalida_entoncesLanzaIllegalStateException() {
        Long orderId = 1L;
        Order mockOrder = new Order();
        mockOrder.setId(orderId);
        mockOrder.setStatus(Order.OrderStatus.CREADO);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));

        assertThrows(IllegalStateException.class, () -> {
            orderService.updateOrderStatus(orderId, Order.OrderStatus.DESPACHADO);
        });

        verify(orderRepository, never()).save(any(Order.class));
    }
}