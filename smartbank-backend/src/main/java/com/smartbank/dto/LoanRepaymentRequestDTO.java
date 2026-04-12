package com.smartbank.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoanRepaymentRequestDTO {

    // Transaction PIN — deducted from disbursement account, PIN required
    @NotBlank(message = "Transaction PIN is required to pay EMI")
    private String pin;
}