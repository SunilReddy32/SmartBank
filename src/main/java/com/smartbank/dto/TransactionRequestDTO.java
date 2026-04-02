package com.smartbank.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class TransactionRequestDTO {

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be greater than 0")
    private Double amount;

    // ✅ FEATURE: PIN required for withdrawals — not required for deposits
    // Validated in TransactionService depending on operation type
    private String pin;
}