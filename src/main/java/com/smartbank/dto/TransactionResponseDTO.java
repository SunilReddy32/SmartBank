package com.smartbank.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TransactionResponseDTO {
    private Long transactionId;
    private Double amount;
    private String type;
    private Long accountId;

    // ✅ NEW: Timestamp now returned in every transaction response
    private LocalDateTime createdAt;
}