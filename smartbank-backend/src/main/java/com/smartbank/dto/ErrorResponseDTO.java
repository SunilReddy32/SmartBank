package com.smartbank.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder

public class ErrorResponseDTO {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private Object message;
}
