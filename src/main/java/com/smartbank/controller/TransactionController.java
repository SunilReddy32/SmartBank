package com.smartbank.controller;

import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

import com.smartbank.service.TransactionService;

import jakarta.validation.Valid;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.smartbank.dto.TransactionRequestDTO;
import com.smartbank.dto.TransactionResponseDTO;
import com.smartbank.dto.TransferRequestDTO;

import java.util.List;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/deposit/{accountId}")
public TransactionResponseDTO deposit(
        @PathVariable Long accountId,
        @Valid @RequestBody TransactionRequestDTO request) {

    return transactionService.deposit(accountId, request.getAmount());
}

@PostMapping("/withdraw/{accountId}")
public TransactionResponseDTO withdraw(
        @PathVariable Long accountId,
        @Valid @RequestBody TransactionRequestDTO request) {

    return transactionService.withdraw(accountId, request.getAmount());
}

@PostMapping("/transfer")
public TransactionResponseDTO transfer(
        @Valid @RequestBody TransferRequestDTO request) {

    return transactionService.transfer(
            request.getFromAccountId(),
            request.getToAccountId(),
            request.getAmount()
    );
}

@GetMapping("/account/{accountId}")
public List<TransactionResponseDTO> getTransactions(
        @PathVariable Long accountId,
        @RequestParam(required = false) String type,
        @RequestParam(defaultValue = "0") int page) {

    Pageable pageable = PageRequest.of(page, 10); // 🔥 size fixed = 10

    return transactionService.getTransactions(accountId, type, pageable);
}
}