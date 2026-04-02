package com.smartbank.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class SetPinRequestDTO {

    @NotBlank(message = "PIN is required")
    @Size(min = 4, max = 6, message = "PIN must be between 4 and 6 digits")
    @Pattern(regexp = "\\d{4,6}", message = "PIN must be 4 to 6 numeric digits")
    private String pin;

    @NotBlank(message = "Confirm PIN is required")
    private String confirmPin;
}