package com.pedidos360.rabbit_admin.service;

import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.stereotype.Service;

@Service
public class RabbitAdminService {

    private final AmqpAdmin amqpAdmin;

    public RabbitAdminService(AmqpAdmin amqpAdmin) {
        this.amqpAdmin = amqpAdmin;
    }

    public void createQueue(String queueName) {
        if (queueName == null || queueName.isBlank()) {
            throw new IllegalArgumentException("El nombre de la cola es obligatorio");
        }
        amqpAdmin.declareQueue(new Queue(queueName, true));
    }

    public void deleteQueue(String queueName) {
        amqpAdmin.deleteQueue(queueName);
    }

    public void createDirectExchange(String exchangeName) {
        amqpAdmin.declareExchange(new DirectExchange(exchangeName));
    }

    public void deleteExchange(String exchangeName) {
        amqpAdmin.deleteExchange(exchangeName);
    }
}