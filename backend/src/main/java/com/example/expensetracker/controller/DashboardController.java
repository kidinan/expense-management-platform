package com.example.expensetracker.controller;

import com.example.expensetracker.dto.DashboardStatsResponse;
import com.example.expensetracker.security.UserPrincipal;
import com.example.expensetracker.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsResponse> getStats(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        DashboardStatsResponse stats = dashboardService.getDashboardStats(userPrincipal.getId());
        return ResponseEntity.ok(stats);
    }
}
