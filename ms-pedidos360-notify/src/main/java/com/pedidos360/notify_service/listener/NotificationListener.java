package com.pedidos360.notify_service.listener;

import com.pedidos360.notify_service.dto.EventEnvelope;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class NotificationListener {

    // 1. Consumidor de Email
    @RabbitListener(queues = "q.cmd.email")
    public void processEmailCommand(EventEnvelope<?> envelope, Channel channel, Message message) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        
        try {
            System.out.println("📩 [EMAIL] Recibido comando de notificación: " + envelope.getType());
            System.out.println("Trace ID: " + envelope.getTraceId());
            System.out.println("Payload: " + envelope.getPayload());

            System.out.println("✅ Correo enviado exitosamente al cliente.");

            // Confirmación explícita (ACK)
            channel.basicAck(deliveryTag, false);

        } catch (Exception e) {
            System.err.println("❌ Error procesando correo. Enviando a q.cmd.email.dlq...");
            // Rechazo explícito (NACK) sin requeue -> va a su DLQ
            channel.basicNack(deliveryTag, false, false);
        }
    }

    // 2. Consumidor de Cocina / Ticket
    @RabbitListener(queues = "q.cmd.kitchen")
    public void processKitchenCommand(EventEnvelope<?> envelope, Channel channel, Message message) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        
        try {
            System.out.println("🍳 [COCINA] Recibida orden de preparación: " + envelope.getType());
            System.out.println("Trace ID: " + envelope.getTraceId());
            System.out.println("Payload: " + envelope.getPayload());

            System.out.println("✅ Ticket impreso en comanda de cocina.");

            // Confirmación explícita (ACK)
            channel.basicAck(deliveryTag, false);

        } catch (Exception e) {
            System.err.println("❌ Error enviando comanda a cocina. Enviando a q.cmd.kitchen.dlq...");
            channel.basicNack(deliveryTag, false, false);
        }
    }

    // 3. Consumidor de Facturación / Boleta
    @RabbitListener(queues = "q.cmd.invoice")
    public void processInvoiceCommand(EventEnvelope<?> envelope, Channel channel, Message message) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        
        try {
            System.out.println("🧾 [FACTURACIÓN] Recibida solicitud de comprobante: " + envelope.getType());
            System.out.println("Trace ID: " + envelope.getTraceId());
            System.out.println("Payload: " + envelope.getPayload());

            System.out.println("✅ Factura/Boleta emitida correctamente.");

            // Confirmación explícita (ACK)
            channel.basicAck(deliveryTag, false);

        } catch (Exception e) {
            System.err.println("❌ Error al generar comprobante. Enviando a q.cmd.invoice.dlq...");
            channel.basicNack(deliveryTag, false, false);
        }
    }
}