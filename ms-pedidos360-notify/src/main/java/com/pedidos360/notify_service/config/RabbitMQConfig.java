package com.pedidos360.notify_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // 1. Exchanges exigidos
    @Bean DirectExchange cmdDirect() { return new DirectExchange("cmd.direct"); }
    @Bean TopicExchange cmdTopic() { return new TopicExchange("cmd.topic"); }
    @Bean DirectExchange cmdDeadDlx() { return new DirectExchange("cmd.dead.dlx"); }

    // 2. Colas DLQ (Dead Letter Queues)
    @Bean Queue emailDlq() { return new Queue("q.cmd.email.dlq"); }
    @Bean Queue kitchenDlq() { return new Queue("q.cmd.kitchen.dlq"); }
    @Bean Queue invoiceDlq() { return new Queue("q.cmd.invoice.dlq"); }

    // 3. Colas Principales enlazadas a sus DLQ
    @Bean Queue emailQueue() {
        return QueueBuilder.durable("q.cmd.email").withArgument("x-dead-letter-exchange", "cmd.dead.dlx").withArgument("x-dead-letter-routing-key", "email.send").build();
    }
    @Bean Queue kitchenQueue() {
        return QueueBuilder.durable("q.cmd.kitchen").withArgument("x-dead-letter-exchange", "cmd.dead.dlx").withArgument("x-dead-letter-routing-key", "kitchen.ticket").build();
    }
    @Bean Queue invoiceQueue() {
        return QueueBuilder.durable("q.cmd.invoice").withArgument("x-dead-letter-exchange", "cmd.dead.dlx").withArgument("x-dead-letter-routing-key", "invoice.gen").build();
    }

    // 4. Bindings Direct (Ruteo exacto)
    @Bean Binding bindEmailDirect() { return BindingBuilder.bind(emailQueue()).to(cmdDirect()).with("email.send"); }
    @Bean Binding bindKitchenDirect() { return BindingBuilder.bind(kitchenQueue()).to(cmdDirect()).with("kitchen.ticket"); }
    @Bean Binding bindInvoiceDirect() { return BindingBuilder.bind(invoiceQueue()).to(cmdDirect()).with("invoice.gen"); }

    // 5. Bindings Topic (Ruteo por patrones)
    @Bean Binding bindEmailTopic() { return BindingBuilder.bind(emailQueue()).to(cmdTopic()).with("email.*"); }
    @Bean Binding bindKitchenTopic() { return BindingBuilder.bind(kitchenQueue()).to(cmdTopic()).with("kitchen.#"); }
    @Bean Binding bindInvoiceTopic() { return BindingBuilder.bind(invoiceQueue()).to(cmdTopic()).with("invoice.*"); }

    // 6. Bindings DLX (Mensajes muertos)
    @Bean Binding bindEmailDlx() { return BindingBuilder.bind(emailDlq()).to(cmdDeadDlx()).with("email.send"); }
    @Bean Binding bindKitchenDlx() { return BindingBuilder.bind(kitchenDlq()).to(cmdDeadDlx()).with("kitchen.ticket"); }
    @Bean Binding bindInvoiceDlx() { return BindingBuilder.bind(invoiceDlq()).to(cmdDeadDlx()).with("invoice.gen"); }

    // 7. Habilitar soporte JSON para RabbitMQ (Mantenemos el tuyo)
    @Bean MessageConverter jsonMessageConverter() { return new Jackson2JsonMessageConverter(); }
}