package com.smartbank.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class TransferByAccountNumberRequestDTO {

    @NotBlank(message = "Sender account number is required")
    private String fromAccountNumber;

    @NotBlank(message = "Receiver account number is required")
    private String toAccountNumber;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be greater than 0")
    private Double amount;

    // ✅ FEATURE: PIN required for all transfers
    @NotBlank(message = "Transaction PIN is required for transfers")
    private String pin;
}