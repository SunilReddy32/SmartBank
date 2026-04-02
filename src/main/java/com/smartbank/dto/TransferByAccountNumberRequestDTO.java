package com.smartbank.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

// Transfer using visible account numbers (like real banking)
// instead of internal database IDs which users never see
@Data
public class TransferByAccountNumberRequestDTO {

    @NotBlank(message = "Sender account number is required")
    private String fromAccountNumber;

    @NotBlank(message = "Receiver account number is required")
    private String toAccountNumber;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be greater than 0")
    private Double amount;
}