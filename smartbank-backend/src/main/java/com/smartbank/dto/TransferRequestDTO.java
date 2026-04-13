package com.smartbank.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class TransferRequestDTO {

    @NotNull(message = "From account ID is required")
    private Long fromAccountId;

    @NotNull(message = "To account ID is required")
    private Long toAccountId;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be greater than 0")
    private Double amount;

    // ✅ FEATURE: PIN required for all transfers
    @NotBlank(message = "Transaction PIN is required for transfers")
    private String pin;
}