package com.sidhant.fraudmanagement.controller;

import com.sidhant.fraudmanagement.dto.response.AdminTransactionResponse;
import com.sidhant.fraudmanagement.service.AdminMonitoringService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminMonitoringService adminMonitoringService;

    public AdminController(
            AdminMonitoringService adminMonitoringService
    ) {
        this.adminMonitoringService = adminMonitoringService;
    }

    @GetMapping("/transactions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AdminTransactionResponse>> getAllTransactions() {

        return ResponseEntity.ok(
                adminMonitoringService.getAllTransactions()
        );
    }
}