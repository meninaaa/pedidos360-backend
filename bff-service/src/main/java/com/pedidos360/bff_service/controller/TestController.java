package com.pedidos360.bff_service.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:4200")
public class TestController {

    @GetMapping("/estado")
    public Object getEstado() {
        RestTemplate restTemplate = new RestTemplate();
        // Restauramos tu ruta original exacta que apunta al microservicio
        String urlMicroservicio = "http://localhost:8081/internos/pedidos";
        
        try {
            return restTemplate.getForObject(urlMicroservicio, Object.class);
        } catch (Exception e) {
            return "{\"error\": \"El BFF validó el token, pero falló la conexión al microservicio: " + e.getMessage() + "\"}";
        }
    }
}