package com.pedidos360.rabbit_admin;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Queue;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RabbitAdminTest {

    @Mock
    private AmqpAdmin amqpAdmin;

    @InjectMocks
    private RabbitAdminService rabbitAdminService;

    // PRUEBA REAL: Crear cola válida -> Se invoca la declaración en el broker correctamente[cite: 10]
    @Test
    void cuandoCrearColaValida_entoncesCreaRecursoExitosamente() {
        String queueName = "q.pedidos360.orders";

        assertDoesNotThrow(() -> {
            rabbitAdminService.createQueue(queueName);
        });

        verify(amqpAdmin, times(1)).declareQueue(any(Queue.class));
    }

    // PRUEBA REAL: Nombre de cola vacío -> Lanza excepción de validación impidiendo la creación[cite: 10]
    @Test
    void cuandoNombreColaVacio_entoncesLanzaExcepcionBadRequest() {
        assertThrows(IllegalArgumentException.class, () -> {
            rabbitAdminService.createQueue("");
        });

        verify(amqpAdmin, never()).declareQueue(any());
    }

    // PRUEBA REAL: Eliminar cola existente -> Operación de borrado ejecutada con éxito[cite: 10]
    @Test
    void cuandoEliminarColaExistente_entoncesBorraRecurso() {
        String queueName = "q.pedidos360.temp";

        assertDoesNotThrow(() -> {
            rabbitAdminService.deleteQueue(queueName);
        });

        verify(amqpAdmin, times(1)).deleteQueue(queueName);
    }
}
