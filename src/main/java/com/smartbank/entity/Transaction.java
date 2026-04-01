package com.smartbank.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private TransactionType type;

    private double amount;

    @ManyToOne
    private Account account;

    // ✅ NEW: auto-stamped when the row is first inserted; never updated
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}