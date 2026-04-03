package com.smartbank.controller;

import com.smartbank.dto.*;
import com.smartbank.service.LoanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;

    // POST /loans/apply
    // Body: { "loanAmount": 100000, "tenureMonths": 12, "disbursementAccountId": 1 }
    // → Creates a PENDING loan application, sends confirmation email
    @PostMapping("/apply")
    public LoanResponseDTO applyLoan(@Valid @RequestBody LoanApplicationRequestDTO request) {
        return loanService.applyLoan(request);
    }

    // GET /loans/my
    // → Returns all loans for the logged-in user with full EMI schedule
    @GetMapping("/my")
    public List<LoanResponseDTO> getMyLoans() {
        return loanService.getMyLoans();
    }

    // GET /loans/{loanId}
    // → Returns a single loan with full EMI schedule (ownership checked)
    @GetMapping("/{loanId}")
    public LoanResponseDTO getLoanById(@PathVariable Long loanId) {
        return loanService.getLoanById(loanId);
    }

    // POST /loans/{loanId}/pay-emi
    // Body: { "pin": "1234" }
    // → Pays the next pending EMI, deducts from disbursement account
    @PostMapping("/{loanId}/pay-emi")
    public LoanRepaymentResponseDTO payEmi(
            @PathVariable Long loanId,
            @Valid @RequestBody LoanRepaymentRequestDTO request) {
        return loanService.payEmi(loanId, request);
    }
}