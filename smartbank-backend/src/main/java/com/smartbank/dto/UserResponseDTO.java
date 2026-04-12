package com.smartbank.dto;

import lombok.Data;

@Data
public class UserResponseDTO {
    private Long id;
    private String name;
    private String email;
    private String role;   // ✅ NEW: "ROLE_USER" or "ROLE_ADMIN"
}