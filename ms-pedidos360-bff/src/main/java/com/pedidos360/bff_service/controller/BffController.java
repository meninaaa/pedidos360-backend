package com.pedidos360.bff_service.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

@RestController
@RequestMapping("/api/bff")
public class BffController {

    private final RestTemplate restTemplate;

    @Value("${CATALOG_SERVICE_URL:http://catalog-pedidos360:8082}")
    private String catalogUrl;

    @Value("${ORDERS_SERVICE_URL:http://orders-pedidos360:8081}")
    private String ordersUrl;

    @Value("${AUDIT_SERVICE_URL:http://audit-pedidos360:8084}")
    private String auditUrl;

    @Value("${REPORT_SERVICE_URL:http://report-pedidos360:8085}")
    private String reportUrl;

    public BffController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping("/catalog/products")
    public ResponseEntity<?> getProducts() {
        return ResponseEntity.ok(restTemplate.getForObject(catalogUrl + "/api/catalog/products", Object.class));
    }

    @GetMapping("/orders")
    public ResponseEntity<?> getOrders() {
        return ResponseEntity.ok(restTemplate.getForObject(ordersUrl + "/api/orders", Object.class));
    }

    @PostMapping("/orders")
    public ResponseEntity<?> createOrder(@RequestBody Map<String, Object> orderRequest) {
        return ResponseEntity.ok(restTemplate.postForObject(ordersUrl + "/api/orders", orderRequest, Object.class));
    }

    @GetMapping("/audit")
    public ResponseEntity<?> getAuditLogs() {
        return ResponseEntity.ok(restTemplate.getForObject(auditUrl + "/api/audit", Object.class));
    }

    @GetMapping("/reports/summary")
    public ResponseEntity<?> getReportSummary() {
        return ResponseEntity.ok(restTemplate.getForObject(reportUrl + "/api/reports/orders-summary", Object.class));
    }
}