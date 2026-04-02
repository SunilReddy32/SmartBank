package com.smartbank.dto;

import com.smartbank.entity.AccountType;
import lombok.Data;

@Data
public class CreateAccountRequestDTO {

    // Optional initial deposit, defaults to 0
    private double initialBalance = 0.0;

    // ✅ NEW: client chooses SAVINGS or CURRENT — defaults to SAVINGS
    private AccountType accountType = AccountType.SAVINGS;
}