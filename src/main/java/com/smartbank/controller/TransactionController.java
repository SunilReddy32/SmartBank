package com.smartbank.controller;

import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

import com.smartbank.service.TransactionService;
import com.smartbank.dto.TransactionRequestDTO;
import com.smartbank.dto.TransactionResponseDTO;
import com.smartbank.dto.TransferRequestDTO;
import com.smartbank.dto.TransferByAccountNumberRequestDTO;

import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    // POST /transactions/deposit/{accountId}
    @PostMapping("/deposit/{accountId}")
    public TransactionResponseDTO deposit(
            @PathVariable Long accountId,
            @Valid @RequestBody TransactionRequestDTO request) {
        return transactionService.deposit(accountId, request.getAmount());
    }

    // POST /transactions/withdraw/{accountId}
    @PostMapping("/withdraw/{accountId}")
    public TransactionResponseDTO withdraw(
            @PathVariable Long accountId,
            @Valid @RequestBody TransactionRequestDTO request) {
        return transactionService.withdraw(accountId, request.getAmount());
    }

    // POST /transactions/transfer — transfer using internal account IDs (kept for compatibility)
    @PostMapping("/transfer")
    public TransactionResponseDTO transfer(
            @Valid @RequestBody TransferRequestDTO request) {
        return transactionService.transfer(
                request.getFromAccountId(),
                request.getToAccountId(),
                request.getAmount()
        );
    }

    // ✅ NEW: POST /transactions/transfer/by-account-number
    // Body: { "fromAccountNumber": "1234567890", "toAccountNumber": "0987654321", "amount": 500 }
    // This is the real-world way users transfer — using the visible account number
    @PostMapping("/transfer/by-account-number")
    public TransactionResponseDTO transferByAccountNumber(
            @Valid @RequestBody TransferByAccountNumberRequestDTO request) {
        return transactionService.transferByAccountNumber(
                request.getFromAccountNumber(),
                request.getToAccountNumber(),
                request.getAmount()
        );
    }

    // GET /transactions/account/{accountId}?type=DEPOSIT&page=0
    @GetMapping("/account/{accountId}")
    public List<TransactionResponseDTO> getTransactions(
            @PathVariable Long accountId,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "0") int page) {
        Pageable pageable = PageRequest.of(page, 10);
        return transactionService.getTransactions(accountId, type, pageable);
    }
}