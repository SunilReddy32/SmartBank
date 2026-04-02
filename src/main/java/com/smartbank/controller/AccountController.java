package com.smartbank.controller;

import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

import com.smartbank.service.AccountService;
import com.smartbank.dto.AccountResponseDTO;
import com.smartbank.dto.CreateAccountRequestDTO;

import java.util.List;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    // ✅ CREATE ACCOUNT
    // BUG FIX: Now uses CreateAccountRequestDTO instead of raw Account entity
    // Account number is auto-generated — client only sends optional initialBalance
    @PostMapping("/create/{userId}")
    public AccountResponseDTO createAccount(
            @PathVariable Long userId,
            @RequestBody CreateAccountRequestDTO request) {

        return accountService.createAccount(userId, request);
    }

    // ✅ GET BALANCE
    @GetMapping("/balance/{accountId}")
    public double getBalance(@PathVariable Long accountId) {
        return accountService.getBalance(accountId);
    }

    // ✅ GET ACCOUNT DETAILS
    @GetMapping("/{accountId}")
    public AccountResponseDTO getAccountDetails(@PathVariable Long accountId) {
        return accountService.getAccountDetails(accountId);
    }

    // ✅ NEW: GET ALL ACCOUNTS FOR A USER
    // Example: GET /accounts/user/3
    @GetMapping("/user/{userId}")
    public List<AccountResponseDTO> getAccountsByUser(@PathVariable Long userId) {
        return accountService.getAccountsByUser(userId);
    }
}