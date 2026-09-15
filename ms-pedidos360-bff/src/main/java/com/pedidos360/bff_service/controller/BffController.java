package com.pedidos360.bff_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
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

    @Autowired
    public BffController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // --- MÉTODO AUXILIAR PARA PROPAGAR EL TOKEN A TODOS LOS MICROSERVICIOS ---
    private HttpEntity<Object> createHttpEntity(Object body, String authHeader) {
        HttpHeaders headers = new HttpHeaders();
        if (authHeader != null) {
            headers.set("Authorization", authHeader);
        }
        return new HttpEntity<>(body, headers);
    }

    // ==========================================
    // MÓDULO DE PEDIDOS (ORDERS)
    // ==========================================

    @GetMapping("/orders")
    public ResponseEntity<?> getOrders(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        HttpEntity<Object> entity = createHttpEntity(null, authHeader);
        return restTemplate.exchange(ordersUrl + "/api/orders", HttpMethod.GET, entity, Object.class);
    }

    // Ruta específica para el Dashboard del Operador (Evita el 404)
    @GetMapping("/orders/pending")
    public ResponseEntity<?> getPendingOrders(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        HttpEntity<Object> entity = createHttpEntity(null, authHeader);
        return restTemplate.exchange(ordersUrl + "/api/orders/pending", HttpMethod.GET, entity, Object.class);
    }

    @PostMapping("/orders")
    public ResponseEntity<?> createOrder(@RequestBody Object order, @RequestHeader(value = "Authorization", required = false) String authHeader) {
        HttpEntity<Object> entity = createHttpEntity(order, authHeader);
        return restTemplate.exchange(ordersUrl + "/api/orders", HttpMethod.POST, entity, Object.class);
    }

    @PutMapping("/orders/{id}/status")
    public ResponseEntity<?> updateOrderStatus(@PathVariable Long id, @RequestParam String nuevoEstado, @RequestHeader(value = "Authorization", required = false) String authHeader) {
        HttpEntity<Object> entity = createHttpEntity(null, authHeader);
        String url = ordersUrl + "/api/orders/" + id + "/status?nuevoEstado=" + nuevoEstado;
        return restTemplate.exchange(url, HttpMethod.PUT, entity, Object.class);
    }


    // ==========================================
    // MÓDULO DE CATÁLOGO (CATALOG)
    // ==========================================

    @GetMapping("/catalog/products")
    public ResponseEntity<?> getProducts(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        HttpEntity<Object> entity = createHttpEntity(null, authHeader);
        return restTemplate.exchange(catalogUrl + "/api/catalog/products", HttpMethod.GET, entity, Object.class);
    }

    @PostMapping("/catalog/products")
    public ResponseEntity<?> createProduct(@RequestBody Map<String, Object> productRequest, @RequestHeader(value = "Authorization", required = false) String authHeader) {
        HttpEntity<Object> entity = createHttpEntity(productRequest, authHeader);
        return restTemplate.exchange(catalogUrl + "/api/catalog/products", HttpMethod.POST, entity, Object.class);
    }

    @PutMapping("/catalog/products/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable Long id, @RequestBody Map<String, Object> productRequest, @RequestHeader(value = "Authorization", required = false) String authHeader) {
        HttpEntity<Object> entity = createHttpEntity(productRequest, authHeader);
        String url = catalogUrl + "/api/catalog/products/" + id;
        return restTemplate.exchange(url, HttpMethod.PUT, entity, Object.class);
    }

    @DeleteMapping("/catalog/products/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id, @RequestHeader(value = "Authorization", required = false) String authHeader) {
        HttpEntity<Object> entity = createHttpEntity(null, authHeader);
        String url = catalogUrl + "/api/catalog/products/" + id;
        return restTemplate.exchange(url, HttpMethod.DELETE, entity, Object.class);
    }


    // ==========================================
    // MÓDULO DE AUDITORÍA (AUDIT)
    // ==========================================

    @GetMapping("/audit")
    public ResponseEntity<?> getAuditLogs(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        HttpEntity<Object> entity = createHttpEntity(null, authHeader);
        return restTemplate.exchange(auditUrl + "/api/audit", HttpMethod.GET, entity, Object.class);
    }


    // ==========================================
    // MÓDULO DE REPORTERÍA (REPORTS)
    // ==========================================

    @GetMapping("/reports/summary")
    public ResponseEntity<?> getReportSummary(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        HttpEntity<Object> entity = createHttpEntity(null, authHeader);
        return restTemplate.exchange(reportUrl + "/api/reports/summary", HttpMethod.GET, entity, Object.class);
    }

    @GetMapping("/reports/ventas-por-hora")
    public ResponseEntity<?> getVentasPorHora(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        HttpEntity<Object> entity = createHttpEntity(null, authHeader);
        return restTemplate.exchange(reportUrl + "/api/reports/ventas-por-hora", HttpMethod.GET, entity, Object.class);
    }

    @GetMapping("/reports/lead-time-trend")
    public ResponseEntity<?> getLeadTimeTrend(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        HttpEntity<Object> entity = createHttpEntity(null, authHeader);
        return restTemplate.exchange(reportUrl + "/api/reports/lead-time-trend", HttpMethod.GET, entity, Object.class);
    }

    @GetMapping("/reports/top-productos")
    public ResponseEntity<?> getTopProductos(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        HttpEntity<Object> entity = createHttpEntity(null, authHeader);
        return restTemplate.exchange(reportUrl + "/api/reports/top-productos", HttpMethod.GET, entity, Object.class);
    }
}