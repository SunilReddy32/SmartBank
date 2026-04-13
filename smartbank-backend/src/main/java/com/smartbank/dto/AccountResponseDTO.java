package com.smartbank.dto;

import lombok.Data;

@Data
public class AccountResponseDTO {
    private Long id;
    private String accountNumber;
    private Double balance;
    private Long userId;
    private String accountType;
    private boolean pinSet;

    // ✅ NEW: daily limits shown in account details
    private double dailyWithdrawalLimit;
    private double dailyTransferLimit;
}