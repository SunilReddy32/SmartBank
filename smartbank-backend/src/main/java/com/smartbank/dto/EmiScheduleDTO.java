package com.smartbank.dto;

import lombok.Data;
import java.time.LocalDate;

// Represents one row in the EMI repayment schedule shown to the user
@Data
public class EmiScheduleDTO {
    private int emiNumber;
    private double emiAmount;
    private LocalDate dueDate;    // expected due date for this EMI
    private String status;        // "PAID" or "PENDING"
    private String paidAt;        // actual payment timestamp if paid, null if pending
}