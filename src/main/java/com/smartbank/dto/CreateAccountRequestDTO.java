package com.smartbank.dto;

import lombok.Data;

@Data
public class CreateAccountRequestDTO {
    // Client only sends an optional initial balance; accountNumber is server-generated
    private double initialBalance = 0.0;
}
