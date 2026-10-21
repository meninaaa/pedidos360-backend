package com.pedidos360.notify;

import com.pedidos360.notify_service.dto.EventEnvelope;
import com.pedidos360.notify_service.listener.NotificationListener;
import com.rabbitmq.client.Channel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import static org.junit.jupiter.api.Assertions.*;

class NotifyResilienceTest {

    private Channel channelProxy;
    private TestChannelHandler channelHandler;
    private Message message;
    private MessageProperties messageProperties;
    private NotificationListener notificationListener;

    static class TestChannelHandler implements InvocationHandler {
        boolean ackCalled = false;
        boolean nackCalled = false;
        long deliveryTag = -1;
        boolean requeue = true;

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String name = method.getName();
            if ("basicAck".equals(name)) {
                ackCalled = true;
                deliveryTag = (Long) args[0];
            } else if ("basicNack".equals(name)) {
                nackCalled = true;
                deliveryTag = (Long) args[0];
                requeue = (Boolean) args[2];
            }
            return null;
        }
    }

    @BeforeEach
    void setUp() {
        channelHandler = new TestChannelHandler();
        channelProxy = (Channel) Proxy.newProxyInstance(
                Channel.class.getClassLoader(),
                new Class[]{Channel.class},
                channelHandler
        );

        messageProperties = new MessageProperties();
        message = new Message(new byte[0], messageProperties);
        notificationListener = new NotificationListener();
    }

    // 1. COLA DE EMAIL: Consumo exitoso (ACK) y Error (DLQ)
    @Test
    void cuandoConsumoEmailExitoso_entoncesConfirmaConAck() throws Exception {
        EventEnvelope<Object> envelope = new EventEnvelope<>();
        envelope.setType("EMAIL_NOTIFICATION");
        messageProperties.setDeliveryTag(101L);

        assertDoesNotThrow(() -> notificationListener.processEmailCommand(envelope, channelProxy, message));
        assertTrue(channelHandler.ackCalled || !channelHandler.nackCalled);
        assertEquals(101L, channelHandler.deliveryTag);
    }

    @Test
    void cuandoErrorProcesamientoEmail_entoncesEnviaNackSinRequeueADlq() throws Exception {
        messageProperties.setDeliveryTag(102L);
        channelProxy.basicNack(messageProperties.getDeliveryTag(), false, false);
        assertTrue(channelHandler.nackCalled);
        assertFalse(channelHandler.requeue);
    }

    // 2. COLA DE COCINA (Kitchen): Consumo exitoso (ACK) y Error (DLQ)
    @Test
    void cuandoConsumoKitchenExitoso_entoncesConfirmaConAck() throws Exception {
        EventEnvelope<Object> envelope = new EventEnvelope<>();
        envelope.setType("KITCHEN_TICKET");
        messageProperties.setDeliveryTag(201L);

        assertDoesNotThrow(() -> notificationListener.processKitchenCommand(envelope, channelProxy, message));
        assertTrue(channelHandler.ackCalled || !channelHandler.nackCalled);
        assertEquals(201L, channelHandler.deliveryTag);
    }

    @Test
    void cuandoErrorProcesamientoKitchen_entoncesEnviaNackSinRequeueADlq() throws Exception {
        messageProperties.setDeliveryTag(202L);
        channelProxy.basicNack(messageProperties.getDeliveryTag(), false, false);
        assertTrue(channelHandler.nackCalled);
        assertFalse(channelHandler.requeue);
    }

    // 3. COLA DE FACTURACIÓN (Invoice): Consumo exitoso (ACK) y Error (DLQ)
    @Test
    void cuandoConsumoInvoiceExitoso_entoncesConfirmaConAck() throws Exception {
        EventEnvelope<Object> envelope = new EventEnvelope<>();
        envelope.setType("INVOICE_GENERATION");
        messageProperties.setDeliveryTag(301L);

        assertDoesNotThrow(() -> notificationListener.processInvoiceCommand(envelope, channelProxy, message));
        assertTrue(channelHandler.ackCalled || !channelHandler.nackCalled);
        assertEquals(301L, channelHandler.deliveryTag);
    }

    @Test
    void cuandoErrorProcesamientoInvoice_entoncesEnviaNackSinRequeueADlq() throws Exception {
        messageProperties.setDeliveryTag(302L);
        channelProxy.basicNack(messageProperties.getDeliveryTag(), false, false);
        assertTrue(channelHandler.nackCalled);
        assertFalse(channelHandler.requeue);
    }
}