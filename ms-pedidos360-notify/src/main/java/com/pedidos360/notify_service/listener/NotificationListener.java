package com.pedidos360.notify_service.listener;

import com.pedidos360.notify_service.dto.EventEnvelope;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class NotificationListener {

    @RabbitListener(queues = "q.cmd.email")
    public void processEmailCommand(EventEnvelope<?> envelope, Channel channel, Message message) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        
        try {
            System.out.println("Recibido comando de notificación: " + envelope.getType());
            System.out.println("Trace ID: " + envelope.getTraceId());
            System.out.println("Payload: " + envelope.getPayload().toString());

            // Simulación de envío de correo
            System.out.println("✅ Correo enviado exitosamente al cliente.");

            // Confirmación explícita (ACK)
            channel.basicAck(deliveryTag, false);

        } catch (Exception e) {
            System.err.println("❌ Error procesando correo. Enviando a DLQ...");
            // Rechazo explícito (NACK) sin re-encolar en la cola principal, va directo a DLQ
            channel.basicNack(deliveryTag, false, false);
        }
    }
}