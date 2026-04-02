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

    // ✅ FEATURE: Account type — SAVINGS earns interest, CURRENT does not
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountType accountType = AccountType.SAVINGS;

    // ✅ FEATURE: Transaction PIN — BCrypt hashed, required before any debit/transfer
    // null means PIN not yet set — user must set it before transacting
    private String transactionPin;
}