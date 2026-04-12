package com.smartbank.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class LoanResponseDTO {

    private Long loanId;
    private Long userId;
    private String userName;

    private String disbursementAccountNumber;

    private double loanAmount;
    private double annualInterestRate;
    private int tenureMonths;

    // Calculated fields
    private double emiAmount;
    private double totalPayable;
    private double totalInterest;

    private int emisPaid;
    private int emisRemaining;

    private String status;           // PENDING / ACTIVE / CLOSED / REJECTED
    private String rejectionReason;  // only set if REJECTED

    private LocalDateTime appliedAt;
    private LocalDate startDate;     // null until approved

    // Full EMI schedule — each row shows due date + PAID/PENDING
    private List<EmiScheduleDTO> emiSchedule;
}