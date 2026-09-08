package com.pedidos360.notify_service.dto;

public class EventEnvelope<T> {
    private String type;
    private String eventId;
    private String timestamp;
    private String traceId;
    private String correlationId;
    private T payload;

    public EventEnvelope() {}

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    public String getTraceId() { return traceId; }
    public void setTraceId(String traceId) { this.traceId = traceId; }
    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
    public T getPayload() { return payload; }
    public void setPayload(T payload) { this.payload = payload; }
}