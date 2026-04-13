package com.smartbank.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "loan_repayments")
public class LoanRepayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Loan loan;

    // Which EMI number this is (1, 2, 3 ... tenureMonths)
    private int emiNumber;

    // Amount paid for this EMI
    private double amount;

    // Timestamp of when this EMI was paid
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime paidAt;
}