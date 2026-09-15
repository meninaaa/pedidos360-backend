package com.pedidos360.bff_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
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

    // --- ENDPOINTS DE ÓRDENES CON PROPAGACIÓN DE TOKEN Y RUTEO INTELIGENTE ---

    @GetMapping("/orders")
    public ResponseEntity<?> getOrders() {
        // 1. Obtenemos la identidad y los roles del usuario autenticado en el BFF
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String roles = auth.getAuthorities().toString(); 

        // 2. Definimos a qué endpoint del microservicio debe ir según su rol
        String targetEndpoint = "/api/orders/me"; // Cliente por defecto

        if (roles.contains("Admin") || roles.contains("ADMIN")) {
            targetEndpoint = "/api/orders"; // Admin ve todo
        } else if (roles.contains("Operador") || roles.contains("OPERADOR")) {
            targetEndpoint = "/api/orders/pending"; // Operador ve pendientes
        }

        // 3. Extraer el token JWT
        JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) auth;
        String tokenValue = jwtAuth.getToken().getTokenValue();

        // 4. Crear las cabeceras con el Bearer Token
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + tokenValue);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // 5. Hacer la llamada al microservicio con la ruta correcta y el token
        ResponseEntity<Object> response = restTemplate.exchange(
            ordersUrl + targetEndpoint, 
            HttpMethod.GET, 
            entity, 
            Object.class
        );

        return ResponseEntity.ok(response.getBody());
    }

    @PostMapping("/orders")
    public ResponseEntity<?> createOrder(@RequestBody Object order) {
        // También propagamos el token al crear el pedido para que no de error 401/500
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) auth;
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + jwtAuth.getToken().getTokenValue());
        HttpEntity<Object> entity = new HttpEntity<>(order, headers);

        String url = ordersUrl + "/api/orders"; 
        ResponseEntity<Object> response = restTemplate.exchange(
            url, 
            HttpMethod.POST, 
            entity, 
            Object.class
        );
        
        return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
    }
}