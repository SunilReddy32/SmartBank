package com.smartbank.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String accountNumber;

    private double balance;

    @ManyToOne
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountType accountType = AccountType.SAVINGS;

    private String transactionPin;

    // ✅ FEATURE: Daily transaction limits
    // 0.0 means no limit set — unlimited
    // Default: ₹50,000/day for withdrawals, ₹1,00,000/day for transfers
    @Column(nullable = false)
    private double dailyWithdrawalLimit = 50000.0;

    @Column(nullable = false)
    private double dailyTransferLimit = 100000.0;
}