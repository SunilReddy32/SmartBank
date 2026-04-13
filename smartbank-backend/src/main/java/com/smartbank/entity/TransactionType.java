package com.smartbank.entity;

public enum TransactionType {
    DEPOSIT,
    WITHDRAW,
    TRANSFER,
    INTEREST_CREDIT,  // monthly savings interest
    LOAN_DISBURSEMENT, // when loan is approved and money credited to account
    EMI_PAYMENT        // when user pays an EMI — debited from their account
}