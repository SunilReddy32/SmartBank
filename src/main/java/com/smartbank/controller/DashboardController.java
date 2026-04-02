package com.smartbank.controller;

import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

import com.smartbank.service.DashboardService;
import com.smartbank.dto.DashboardResponseDTO;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    // GET /dashboard/{userId}
    // Returns: userName, totalAccounts, totalBalance, all accounts, last 5 transactions
    // One call — the frontend homepage doesn't need multiple API requests
    @GetMapping("/{userId}")
    public DashboardResponseDTO getDashboard(@PathVariable Long userId) {
        return dashboardService.getDashboard(userId);
    }
}