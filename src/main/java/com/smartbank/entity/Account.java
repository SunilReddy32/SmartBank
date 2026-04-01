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

    // ✅ unique + auto-generated server-side; client never sends this
    @Column(unique = true, nullable = false)
    private String accountNumber;

    private double balance;

    @ManyToOne
    private User user;
}