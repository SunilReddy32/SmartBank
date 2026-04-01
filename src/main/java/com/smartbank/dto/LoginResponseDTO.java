package com.smartbank.dto;

import lombok.Data;

@Data
public class LoginResponseDTO {
    private String message;
    private String email;
    private String token;
}
