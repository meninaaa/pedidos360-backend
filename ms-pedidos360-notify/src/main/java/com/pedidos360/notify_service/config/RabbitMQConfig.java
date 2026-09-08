package com.pedidos360.notify_service.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // 1. Exchanges
    @Bean
    public DirectExchange cmdDirectExchange() {
        return new DirectExchange("cmd.direct");
    }

    @Bean
    public DirectExchange cmdDeadExchange() {
        return new DirectExchange("cmd.dead.dlx");
    }

    // 2. Colas (Principal y DLQ)
    @Bean
    public Queue emailDlq() {
        return QueueBuilder.durable("q.cmd.email.dlq").build();
    }

    @Bean
    public Queue emailQueue() {
        return QueueBuilder.durable("q.cmd.email")
                .withArgument("x-dead-letter-exchange", "cmd.dead.dlx")
                .withArgument("x-dead-letter-routing-key", "email.send")
                .build();
    }

    // 3. Bindings
    @Bean
    public Binding emailBinding(Queue emailQueue, DirectExchange cmdDirectExchange) {
        return BindingBuilder.bind(emailQueue).to(cmdDirectExchange).with("email.send");
    }

    @Bean
    public Binding emailDlqBinding(Queue emailDlq, DirectExchange cmdDeadExchange) {
        return BindingBuilder.bind(emailDlq).to(cmdDeadExchange).with("email.send");
    }
}