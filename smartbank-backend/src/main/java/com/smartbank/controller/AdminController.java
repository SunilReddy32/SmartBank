package com.smartbank.controller;

import com.smartbank.dto.*;
import com.smartbank.service.AdminService;
import com.smartbank.service.LoanService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final LoanService loanService;

    // ── User management ──────────────────────────────────────────────────────

    @GetMapping("/users")
    public List<UserResponseDTO> getAllUsers() {
        return adminService.getAllUsers();
    }

    @GetMapping("/users/{userId}")
    public UserResponseDTO getUserById(@PathVariable Long userId) {
        return adminService.getUserById(userId);
    }

    @GetMapping("/accounts")
    public List<AccountResponseDTO> getAllAccounts() {
        return adminService.getAllAccounts();
    }

    @GetMapping("/accounts/user/{userId}")
    public List<AccountResponseDTO> getAccountsByUser(@PathVariable Long userId) {
        return adminService.getAccountsByUser(userId);
    }

    @PutMapping("/users/{userId}/role")
    public UserResponseDTO updateUserRole(
            @PathVariable Long userId,
            @RequestParam String role) {
        return adminService.updateUserRole(userId, role);
    }

    // ── Loan management ───────────────────────────────────────────────────────

    // GET /admin/loans — all loans in the system
    @GetMapping("/loans")
    public List<LoanResponseDTO> getAllLoans() {
        return loanService.getAllLoans();
    }

    // GET /admin/loans/pending — only PENDING applications waiting for review
    @GetMapping("/loans/pending")
    public List<LoanResponseDTO> getPendingLoans() {
        return loanService.getPendingLoans();
    }

    // GET /admin/loans/{loanId} — view any loan's full details + EMI schedule
    @GetMapping("/loans/{loanId}")
    public LoanResponseDTO getLoanById(@PathVariable Long loanId) {
        return loanService.getLoanById(loanId);
    }

    // PUT /admin/loans/{loanId}/approve
    // → Sets status to ACTIVE, credits loan amount to account, sends approval email
    @PutMapping("/loans/{loanId}/approve")
    public LoanResponseDTO approveLoan(@PathVariable Long loanId) {
        return loanService.approveLoan(loanId);
    }

    // PUT /admin/loans/{loanId}/reject?reason=Low+credit+score
    // → Sets status to REJECTED, sends rejection email with reason
    @PutMapping("/loans/{loanId}/reject")
    public LoanResponseDTO rejectLoan(
            @PathVariable Long loanId,
            @RequestParam String reason) {
        return loanService.rejectLoan(loanId, reason);
    }
}