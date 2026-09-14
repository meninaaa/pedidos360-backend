package com.pedidos360.report_service.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "report_logs")
public class OrderEventLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long orderId;
    private String customerId;
    private String status;
    private Double total;
    private Instant eventTimestamp;

    public OrderEventLog() {}

    public OrderEventLog(Long orderId, String customerId, String status, Double total, Instant eventTimestamp) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.status = status;
        this.total = total;
        this.eventTimestamp = eventTimestamp;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Double getTotal() { return total; }
    public void setTotal(Double total) { this.total = total; }
    public Instant getEventTimestamp() { return eventTimestamp; }
    public void setEventTimestamp(Instant eventTimestamp) { this.eventTimestamp = eventTimestamp; }
}