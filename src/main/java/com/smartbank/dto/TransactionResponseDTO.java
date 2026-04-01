package com.smartbank.dto;

import lombok.Data;

@Data
public class TransactionResponseDTO {
    private Long transactionId;
    private Double amount;
    private String type;
    private Long accountId;
}
