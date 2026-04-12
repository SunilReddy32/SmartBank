package com.smartbank.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class LoanRepaymentResponseDTO {
    private Long repaymentId;
    private Long loanId;
    private int emiNumber;
    private double amount;
    private LocalDateTime paidAt;
    private int emisPaid;
    private int emisRemaining;
    private String loanStatus;   // ACTIVE or CLOSED (if this was the last EMI)
    private double accountBalanceAfterPayment;
}