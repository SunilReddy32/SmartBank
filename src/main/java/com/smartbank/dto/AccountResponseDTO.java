package com.smartbank.dto;

import lombok.Data;

@Data
public class AccountResponseDTO {
    private Long id;
    private String accountNumber;
    private Double balance;
    private Long userId;
}
