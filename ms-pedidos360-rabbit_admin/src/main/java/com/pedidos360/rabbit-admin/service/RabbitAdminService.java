package com.pedidos360.admin_service.service;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.stereotype.Service;

@Service
public class RabbitAdminService {

    private final RabbitAdmin rabbitAdmin;

    public RabbitAdminService(RabbitAdmin rabbitAdmin) {
        this.rabbitAdmin = rabbitAdmin;
    }

    public void createQueue(String queueName) {
        Queue queue = new Queue(queueName, true);
        rabbitAdmin.declareQueue(queue);
    }

    public void deleteQueue(String queueName) {
        rabbitAdmin.deleteQueue(queueName);
    }

    public void createDirectExchange(String exchangeName) {
        DirectExchange exchange = new DirectExchange(exchangeName);
        rabbitAdmin.declareExchange(exchange);
    }

    public void deleteExchange(String exchangeName) {
        rabbitAdmin.deleteExchange(exchangeName);
    }
}