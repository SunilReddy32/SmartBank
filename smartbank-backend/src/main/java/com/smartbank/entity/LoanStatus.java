package com.smartbank.entity;

public enum LoanStatus {
    PENDING,    // user applied, waiting for admin approval
    ACTIVE,     // admin approved, money disbursed, EMIs running
    CLOSED,     // all EMIs paid off
    REJECTED    // admin rejected the application
}