package com.smartbank.dto;

import lombok.Data;
import java.util.Map;

@Data
public class MonthlyBreakdownDTO {
    private int year;
    private int month;
    private String monthLabel;       // e.g. "Jan 2025" — for chart labels
    private Map<String, Double> byType; // { "DEPOSIT": 5000, "WITHDRAW": 2000, ... }
}