package com.smartbank.dto;

import lombok.Data;

@Data
public class LoginResponseDTO {
    private String message;
    private String email;
    private String token;
    private String role;
    private Long userId;   // ✅ ADDED — frontend needs this for all API calls
    private String name;   // ✅ ADDED — frontend shows user name in sidebar
}