package com.smartbank.dto;

import lombok.Data;

// ✅ NEW DTO: Replaces the raw Account entity being accepted in the controller
// Account number is now auto-generated server-side — client doesn't send it
@Data
public class CreateAccountRequestDTO {

    // Optional: client can send initial deposit amount (defaults to 0)
    private double initialBalance = 0.0;
}