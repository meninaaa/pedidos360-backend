package com.pedidos360.report_service.controller;

import com.pedidos360.report_service.entity.Report;
import com.pedidos360.report_service.repository.ReportRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportRepository reportRepository;

    public ReportController(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    @GetMapping("/orders-summary")
    public List<Report> getReportSummary() {
        return reportRepository.findAll();
    }
}