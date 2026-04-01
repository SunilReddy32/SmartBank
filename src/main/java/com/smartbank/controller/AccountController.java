package com.smartbank.controller;

import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import com.smartbank.service.AccountService;
import com.smartbank.dto.AccountResponseDTO;
import com.smartbank.entity.Account;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping("/create/{userId}")
    public AccountResponseDTO createAccount(
            @PathVariable Long userId,
            @RequestBody Account account) {

        return accountService.createAccount(userId, account);
    }

    @GetMapping("/balance/{accountId}")
public double getBalance(@PathVariable Long accountId) {
    return accountService.getBalance(accountId);
}

@GetMapping("/{accountId}")
public AccountResponseDTO getAccountDetails(@PathVariable Long accountId) {
    return accountService.getAccountDetails(accountId);
}
}