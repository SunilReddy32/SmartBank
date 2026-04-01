package com.smartbank.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UpdateUserRequestDTO {
    
    private String name;

    @Email(message = "Invalid email format")
    private String email;

    @Size(min = 4, message = "Password must be at least 4 characters")
    private String password;
}
