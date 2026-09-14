package com.pedidos360.report_service.controller;

import com.pedidos360.report_service.service.ReportMetricsService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportMetricsService metricsService;

    public ReportController(ReportMetricsService metricsService) {
        this.metricsService = metricsService;
    }

    @GetMapping("/summary")
    @PreAuthorize("hasRole('Admin')")
    public Map<String, Object> getDashboardSummary() {
        return metricsService.getMetrics();
    }

    @GetMapping("/ventas-por-hora")
    @PreAuthorize("hasRole('Admin')")
    public List<Map<String, Object>> getVentasPorHora() {
        return metricsService.getVentasPorHora();
    }

    @GetMapping("/lead-time-trend")
    @PreAuthorize("hasRole('Admin')")
    public List<Map<String, Object>> getLeadTimeTrend() {
        return metricsService.getLeadTimeTrend();
    }

    @GetMapping("/top-productos")
    @PreAuthorize("hasRole('Admin')")
    public List<Map<String, Object>> getTopProductos() {
        return metricsService.getTopProductos();
    }
}