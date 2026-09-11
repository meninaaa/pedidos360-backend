package com.pedidos360.report_service.controller;

import com.pedidos360.report_service.repository.ReportRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportRepository reportRepository;

    public ReportController(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    @GetMapping("/orders-summary")
    public ResponseEntity<List<Map<String, Object>>> getOrdersSummary() {
        List<Object[]> results = reportRepository.getOrdersSummaryByStatus();
        List<Map<String, Object>> report = new ArrayList<>();
        
        for (Object[] row : results) {
            Map<String, Object> map = new HashMap<>();
            map.put("status", row[0]);
            map.put("total", row[1]);
            report.add(map);
        }
        
        return ResponseEntity.ok(report);
    }
}