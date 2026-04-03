package com.smartbank.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "loans")
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Who applied for the loan
    @ManyToOne
    @JoinColumn(nullable = false)
    private User user;

    // Which account the loan amount will be disbursed into
    @ManyToOne
    @JoinColumn(nullable = false)
    private Account disbursementAccount;

    // Principal amount requested
    @Column(nullable = false)
    private double loanAmount;

    // Annual interest rate (e.g. 10.0 = 10%)
    @Column(nullable = false)
    private double annualInterestRate;

    // Loan duration in months (e.g. 12, 24, 36)
    @Column(nullable = false)
    private int tenureMonths;

    // Calculated EMI — set when loan is approved
    private double emiAmount;

    // Total amount payable = EMI × tenureMonths
    private double totalPayable;

    // Total interest = totalPayable - loanAmount
    private double totalInterest;

    // How many EMIs have been paid so far
    private int emisPaid = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoanStatus status = LoanStatus.PENDING;

    // Reason if rejected by admin
    private String rejectionReason;

    // When user applied
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime appliedAt;

    // When admin approved and disbursed
    private LocalDate startDate;

    // All EMI repayment records
    @OneToMany(mappedBy = "loan", cascade = CascadeType.ALL)
    private List<LoanRepayment> repayments;

    public int getEmisRemaining() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getEmisRemaining'");
    }
}