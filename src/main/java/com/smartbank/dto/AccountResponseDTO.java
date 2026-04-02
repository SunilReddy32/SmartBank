package com.smartbank.dto;

import lombok.Data;

@Data
public class AccountResponseDTO {
    private Long id;
    private String accountNumber;
    private Double balance;
    private Long userId;

    // ✅ NEW: account type shown in responses
    private String accountType;

    // ✅ NEW: tells frontend whether PIN has been set (true/false)
    // Never expose the actual PIN hash
    private boolean pinSet;
}   