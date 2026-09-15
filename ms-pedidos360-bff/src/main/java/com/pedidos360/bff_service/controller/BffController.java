package com.pedidos360.bff_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;

import java.util.List;
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

    @GetMapping("/catalog/products")
    public ResponseEntity<?> getProducts() {
        return ResponseEntity.ok(restTemplate.getForObject(catalogUrl + "/api/catalog/products", Object.class));
    }


    @GetMapping("/audit")
    public ResponseEntity<?> getAuditLogs() {
        return ResponseEntity.ok(restTemplate.getForObject(auditUrl + "/api/audit", Object.class));
    }

    @GetMapping("/reports/summary")
    public ResponseEntity<?> getReportSummary() {
        // Ajustado para que coincida exactamente con la ruta configurada en el ReportController
        return ResponseEntity.ok(restTemplate.getForObject(reportUrl + "/api/reports/summary", Object.class));
    }

    @PutMapping("/orders/{id}/status")
    public ResponseEntity<?> updateOrderStatus(@PathVariable Long id, @RequestParam String nuevoEstado) {
        String url = ordersUrl + "/api/orders/" + id + "/status?nuevoEstado=" + nuevoEstado;
        restTemplate.put(url, null);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/catalog/products")
    public ResponseEntity<?> createProduct(@RequestBody Map<String, Object> productRequest) {
        return ResponseEntity.ok(restTemplate.postForObject(catalogUrl + "/api/catalog/products", productRequest, Object.class));
    }

    @PutMapping("/catalog/products/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable Long id, @RequestBody Map<String, Object> productRequest) {
        String url = catalogUrl + "/api/catalog/products/" + id;
        restTemplate.put(url, productRequest);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/catalog/products/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        String url = catalogUrl + "/api/catalog/products/" + id;
        restTemplate.delete(url);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/reports/ventas-por-hora")
    public ResponseEntity<?> getVentasPorHora() {
        String url = reportUrl + "/api/reports/ventas-por-hora";
        return restTemplate.getForEntity(url, List.class);
    }

    @GetMapping("/reports/lead-time-trend")
    public ResponseEntity<?> getLeadTimeTrend() {
        String url = reportUrl + "/api/reports/lead-time-trend";
        return restTemplate.getForEntity(url, List.class);
    }

    @GetMapping("/reports/top-productos")
    public ResponseEntity<?> getTopProductos() {
        String url = reportUrl + "/api/reports/top-productos";
        return restTemplate.getForEntity(url, List.class);
    }

     @GetMapping("/orders")
        public ResponseEntity<?> getOrders() {
        // 1. Obtenemos la identidad y los roles del usuario autenticado en el BFF
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String roles = auth.getAuthorities().toString(); // Se verá como "[ROLE_Cliente]"

        // 2. Definimos a qué endpoint del microservicio debe ir según su rol
        String targetEndpoint = "/api/orders/me"; // Por defecto, asumimos que es Cliente y solo ve los suyos

        if (roles.contains("ROLE_Admin")) {
            targetEndpoint = "/api/orders"; // El admin ve todo
        } else if (roles.contains("ROLE_Operador")) {
            targetEndpoint = "/api/orders/pending"; // El operador ve los pendientes
        }

        // 3. Hacemos la llamada al microservicio con la ruta correcta
        return ResponseEntity.ok(restTemplate.getForObject(ordersUrl + targetEndpoint, Object.class));
    }
}