package com.pedidos360.bff_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/bff")
public class BffController {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

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
        String targetEndpoint = "/api/orders/me"; 

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String token = authHeader.substring(7);
                String[] chunks = token.split("\\.");
                if (chunks.length > 1) {
                    String payload = new String(Base64.getUrlDecoder().decode(chunks[1]));
                    Map<String, Object> claims = objectMapper.readValue(payload, Map.class);
                    
                    // CORREGIDO: Uso correcto de paréntesis en claims.get("roles")
                    String rolesStr = claims.get("roles") != null ? claims.get("roles").toString() : "";
                    if (rolesStr.contains("Admin") || rolesStr.contains("Administrador")) {
                        targetEndpoint = "/api/orders"; 
                    } else if (rolesStr.contains("Operador") || rolesStr.contains("Operador de Logística")) {
                        targetEndpoint = "/api/orders/pending"; 
                    }
                }
            } catch (Exception e) {
                System.out.println("Advertencia: No se pudo decodificar el token en /orders: " + e.getMessage());
            }
        }

        HttpEntity<Object> entity = createHttpEntity(null, authHeader);
        return restTemplate.exchange(ordersUrl + targetEndpoint, HttpMethod.GET, entity, Object.class);
    }

    @GetMapping("/orders/pending")
    public ResponseEntity<?> getPendingOrders(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        HttpEntity<Object> entity = createHttpEntity(null, authHeader);
        return restTemplate.exchange(ordersUrl + "/api/orders/pending", HttpMethod.GET, entity, Object.class);
    }

    @GetMapping("/orders/me")
    public ResponseEntity<?> getMyOrders(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            String userEmail = "";
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                String[] chunks = token.split("\\.");
                if (chunks.length > 1) {
                    String payload = new String(Base64.getUrlDecoder().decode(chunks[1]));
                    Map<String, Object> claims = objectMapper.readValue(payload, Map.class);
                    
                    if (claims.containsKey("preferred_username")) {
                        userEmail = claims.get("preferred_username").toString();
                    } else if (claims.containsKey("email")) {
                        userEmail = claims.get("email").toString();
                    } else if (claims.containsKey("upn")) {
                        userEmail = claims.get("upn").toString();
                    } else if (claims.containsKey("unique_name")) {
                        userEmail = claims.get("unique_name").toString();
                    }
                }
            }

            HttpEntity<Object> entity = createHttpEntity(null, authHeader);
            ResponseEntity<List> response = restTemplate.exchange(ordersUrl + "/api/orders", HttpMethod.GET, entity, List.class);
            
            List<Map<String, Object>> allOrders = (List<Map<String, Object>>) response.getBody();
            if (allOrders == null) {
                return ResponseEntity.ok(Collections.emptyList());
            }

            if (userEmail != null && !userEmail.isEmpty()) {
                String finalEmail = userEmail.trim();
                List<Map<String, Object>> filteredOrders = allOrders.stream()
                    .filter(order -> {
                        Object custId = order.get("customerId");
                        if (custId == null) return false;
                        String customerStr = custId.toString().trim();
                        return customerStr.equalsIgnoreCase(finalEmail) || customerStr.toLowerCase().contains(finalEmail.toLowerCase());
                    })
                    .collect(Collectors.toList());
                return ResponseEntity.ok(filteredOrders);
            }

            return ResponseEntity.ok(allOrders);
        } catch (Exception e) {
            System.out.println("Error procesando /orders/me en el BFF: " + e.getMessage());
            HttpEntity<Object> entity = createHttpEntity(null, authHeader);
            return restTemplate.exchange(ordersUrl + "/api/orders/me", HttpMethod.GET, entity, Object.class);
        }
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