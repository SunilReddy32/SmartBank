package com.smartbank.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class LoanApplicationRequestDTO {

    @NotNull(message = "Loan amount is required")
    @Positive(message = "Loan amount must be greater than 0")
    @Min(value = 1000, message = "Minimum loan amount is ₹1,000")
    @Max(value = 10000000, message = "Maximum loan amount is ₹1,00,00,000")
    private Double loanAmount;

    @NotNull(message = "Tenure is required")
    @Min(value = 3, message = "Minimum tenure is 3 months")
    @Max(value = 360, message = "Maximum tenure is 360 months (30 years)")
    private Integer tenureMonths;

    // Account ID where loan money will be disbursed
    @NotNull(message = "Disbursement account ID is required")
    private Long disbursementAccountId;
}