package com.smartbank.controller;

import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

import jakarta.validation.Valid;

import com.smartbank.service.AccountService;
import com.smartbank.dto.AccountResponseDTO;
import com.smartbank.dto.CreateAccountRequestDTO;
import com.smartbank.dto.SetPinRequestDTO;

import java.util.List;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    // POST /accounts/create/{userId}
    // Body: { "initialBalance": 1000, "accountType": "SAVINGS" }
    @PostMapping("/create/{userId}")
    public AccountResponseDTO createAccount(
            @PathVariable Long userId,
            @RequestBody CreateAccountRequestDTO request) {
        return accountService.createAccount(userId, request);
    }

    // GET /accounts/balance/{accountId}
    @GetMapping("/balance/{accountId}")
    public double getBalance(@PathVariable Long accountId) {
        return accountService.getBalance(accountId);
    }

    // GET /accounts/{accountId}
    @GetMapping("/{accountId}")
    public AccountResponseDTO getAccountDetails(@PathVariable Long accountId) {
        return accountService.getAccountDetails(accountId);
    }

    // GET /accounts/user/{userId}
    @GetMapping("/user/{userId}")
    public List<AccountResponseDTO> getAccountsByUser(@PathVariable Long userId) {
        return accountService.getAccountsByUser(userId);
    }

    // ✅ FEATURE: SET TRANSACTION PIN
    // PUT /accounts/{accountId}/pin
    // Body: { "pin": "1234", "confirmPin": "1234" }
    // Must be called once before user can withdraw or transfer
    @PutMapping("/{accountId}/pin")
    public String setTransactionPin(
            @PathVariable Long accountId,
            @Valid @RequestBody SetPinRequestDTO request) {
        return accountService.setTransactionPin(accountId, request);
    }
}