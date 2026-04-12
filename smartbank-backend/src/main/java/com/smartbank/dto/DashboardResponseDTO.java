package com.smartbank.dto;

import lombok.Data;
import java.util.List;

@Data
public class DashboardResponseDTO {

    private Long userId;
    private String userName;

    // Totals shown at the top of the frontend dashboard
    private int totalAccounts;
    private double totalBalance;

    // All accounts with individual balances
    private List<AccountResponseDTO> accounts;

    // Last 5 transactions across ALL accounts of this user
    private List<TransactionResponseDTO> recentTransactions;
}