package com.smartbank.controller;

import com.smartbank.dto.SpendingAnalyticsResponseDTO;
import com.smartbank.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    // GET /analytics/{userId}?months=6
    // Returns summary totals + month-by-month breakdown — ready for frontend charts
    // months param: 1–12, defaults to 6
    @GetMapping("/{userId}")
    public SpendingAnalyticsResponseDTO getAnalytics(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "6") int months) {

        // Cap at 12 months max
        if (months < 1) months = 1;
        if (months > 12) months = 12;

        return analyticsService.getAnalytics(userId, months);
    }
}