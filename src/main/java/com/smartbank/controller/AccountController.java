package com.smartbank.controller;

import com.smartbank.dto.AccountResponseDTO;
import com.smartbank.dto.CreateAccountRequestDTO;
import com.smartbank.dto.SetDailyLimitRequestDTO;
import com.smartbank.dto.SetPinRequestDTO;
import com.smartbank.service.AccountService;
import com.smartbank.service.StatementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final StatementService statementService;

    @PostMapping("/create/{userId}")
    public AccountResponseDTO createAccount(@PathVariable Long userId,
                                             @RequestBody CreateAccountRequestDTO request) {
        return accountService.createAccount(userId, request);
    }

    @GetMapping("/balance/{accountId}")
    public double getBalance(@PathVariable Long accountId) {
        return accountService.getBalance(accountId);
    }

    @GetMapping("/{accountId}")
    public AccountResponseDTO getAccountDetails(@PathVariable Long accountId) {
        return accountService.getAccountDetails(accountId);
    }

    @GetMapping("/user/{userId}")
    public List<AccountResponseDTO> getAccountsByUser(@PathVariable Long userId) {
        return accountService.getAccountsByUser(userId);
    }

    // PUT /accounts/{accountId}/pin
    @PutMapping("/{accountId}/pin")
    public String setTransactionPin(@PathVariable Long accountId,
                                     @Valid @RequestBody SetPinRequestDTO request) {
        return accountService.setTransactionPin(accountId, request);
    }

    // ✅ FEATURE: Set daily limits
    // PUT /accounts/{accountId}/limits
    // Body: { "dailyWithdrawalLimit": 25000, "dailyTransferLimit": 50000 }
    // Set to 0 to remove the limit (unlimited)
    @PutMapping("/{accountId}/limits")
    public AccountResponseDTO setDailyLimits(@PathVariable Long accountId,
                                              @Valid @RequestBody SetDailyLimitRequestDTO request) {
        return accountService.setDailyLimits(accountId, request);
    }

    // ✅ FEATURE: Download account statement as PDF
    // GET /accounts/{accountId}/statement?fromYear=2025&fromMonth=1&toYear=2025&toMonth=3
    // Returns a downloadable PDF with all transactions in the period
    @GetMapping("/{accountId}/statement")
    public ResponseEntity<byte[]> downloadStatement(
            @PathVariable Long accountId,
            @RequestParam int fromYear,
            @RequestParam int fromMonth,
            @RequestParam int toYear,
            @RequestParam int toMonth) {

        byte[] pdf = statementService.generateStatement(
                accountId, fromYear, fromMonth, toYear, toMonth);

        String filename = "SmartBank_Statement_" + accountId + "_" +
                fromYear + fromMonth + "_to_" + toYear + toMonth + ".pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}