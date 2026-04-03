package com.smartbank.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class SetDailyLimitRequestDTO {

    // 0 = unlimited, positive = limit in rupees
    @Min(value = 0, message = "Daily withdrawal limit cannot be negative")
    private double dailyWithdrawalLimit;

    @Min(value = 0, message = "Daily transfer limit cannot be negative")
    private double dailyTransferLimit;
}