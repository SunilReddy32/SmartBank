package com.smartbank.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class TransactionRequestDTO {

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be greater than 0")
    private Double amount;
}
