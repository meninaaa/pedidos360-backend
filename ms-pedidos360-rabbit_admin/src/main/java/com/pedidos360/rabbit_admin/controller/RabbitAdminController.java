package com.pedidos360.rabbit_admin.controller;

import com.pedidos360.rabbit_admin.service.RabbitAdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/rabbit")
public class RabbitAdminController {

    private final RabbitAdminService adminService;

    public RabbitAdminController(RabbitAdminService adminService) {
        this.adminService = adminService;
    }

  @PostMapping("/queues")
public ResponseEntity<?> createQueue(@RequestBody Map<String, String> body) {
    String name = body.get("name");
    if (name == null || name.trim().isEmpty()) {
        return ResponseEntity.badRequest().body(Map.of("error", "El nombre de la cola no puede estar vacío"));
    }
    adminService.createQueue(name);
    return ResponseEntity.ok(Map.of("message", "Cola creada exitosamente: " + name));
}

@DeleteMapping("/queues/{name}")
public ResponseEntity<?> deleteQueue(@PathVariable String name) {
    if (name == null || name.trim().isEmpty()) {
        return ResponseEntity.badRequest().body(Map.of("error", "Nombre de cola inválido"));
    }
    adminService.deleteQueue(name);
    return ResponseEntity.ok(Map.of("message", "Cola eliminada: " + name));
}
}