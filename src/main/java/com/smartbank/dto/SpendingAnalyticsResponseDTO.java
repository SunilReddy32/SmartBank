package com.smartbank.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class SpendingAnalyticsResponseDTO {

    private Long userId;
    private String userName;
    private String period;           // e.g. "Last 6 months"

    // Summary totals across all accounts
    private double totalDeposited;   // sum of all DEPOSIT transactions
    private double totalWithdrawn;   // sum of all WITHDRAW transactions
    private double totalTransferred; // sum of all TRANSFER (sent) transactions
    private double totalInterestEarned;
    private double totalEmiPaid;
    private double netFlow;          // totalDeposited - totalWithdrawn - totalTransferred

    // Breakdown by transaction type (for pie chart on frontend)
    private Map<String, Double> breakdownByType;

    // Month-by-month breakdown (for bar/line chart)
    private List<MonthlyBreakdownDTO> monthlyBreakdown;
}