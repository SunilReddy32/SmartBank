package com.smartbank.service;

import com.smartbank.dto.*;
import com.smartbank.entity.*;
import com.smartbank.exception.AccountNotFoundException;
import com.smartbank.exception.LoanNotFoundException;
import com.smartbank.exception.UserNotFoundException;
import com.smartbank.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LoanService {

    private final LoanRepository loanRepository;
    private final LoanRepaymentRepository loanRepaymentRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final AccountService accountService;
    private final EmailService emailService;

    // Default loan interest rate from config — admin can override per application in future
    @Value("${smartbank.loan.annual-rate:10.0}")
    private double defaultLoanInterestRate;

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private User getLoggedInUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Logged-in user not found"));
    }

    /**
     * EMI Formula: P × r × (1+r)^n / ((1+r)^n - 1)
     * P = principal, r = monthly interest rate, n = tenure in months
     */
    private double calculateEmi(double principal, double annualRate, int tenureMonths) {
        double monthlyRate = annualRate / 100.0 / 12.0;
        if (monthlyRate == 0) return principal / tenureMonths; // 0% interest edge case
        double factor = Math.pow(1 + monthlyRate, tenureMonths);
        double emi = principal * monthlyRate * factor / (factor - 1);
        return Math.round(emi * 100.0) / 100.0; // round to 2 decimal places
    }

    private LoanResponseDTO toDTO(Loan loan) {
        List<LoanRepayment> repayments = loanRepaymentRepository.findByLoanOrderByEmiNumberAsc(loan);

        LoanResponseDTO dto = new LoanResponseDTO();
        dto.setLoanId(loan.getId());
        dto.setUserId(loan.getUser().getId());
        dto.setUserName(loan.getUser().getName());
        dto.setDisbursementAccountNumber(loan.getDisbursementAccount().getAccountNumber());
        dto.setLoanAmount(loan.getLoanAmount());
        dto.setAnnualInterestRate(loan.getAnnualInterestRate());
        dto.setTenureMonths(loan.getTenureMonths());
        dto.setEmiAmount(loan.getEmiAmount());
        dto.setTotalPayable(loan.getTotalPayable());
        dto.setTotalInterest(loan.getTotalInterest());
        dto.setEmisRemaining(loan.getTenureMonths() - loan.getEmisRemaining());
        dto.setEmisRemaining(loan.getTenureMonths() - loan.getEmisPaid());
        dto.setEmisPaid(loan.getEmisPaid());
        dto.setStatus(loan.getStatus().name());
        dto.setRejectionReason(loan.getRejectionReason());
        dto.setAppliedAt(loan.getAppliedAt());
        dto.setStartDate(loan.getStartDate());

        // Build EMI schedule — shows all EMIs with PAID/PENDING status
        List<EmiScheduleDTO> schedule = new ArrayList<>();
        for (int i = 1; i <= loan.getTenureMonths(); i++) {
            EmiScheduleDTO emi = new EmiScheduleDTO();
            emi.setEmiNumber(i);
            emi.setEmiAmount(loan.getEmiAmount());

            // Due date = startDate + i months (null until loan is approved)
            if (loan.getStartDate() != null) {
                emi.setDueDate(loan.getStartDate().plusMonths(i));
            }

            // Check if this EMI has been paid
            final int emiNum = i;
            repayments.stream()
                    .filter(r -> r.getEmiNumber() == emiNum)
                    .findFirst()
                    .ifPresentOrElse(
                        r -> {
                            emi.setStatus("PAID");
                            emi.setPaidAt(r.getPaidAt().toString());
                        },
                        () -> emi.setStatus("PENDING")
                    );

            schedule.add(emi);
        }

        dto.setEmiSchedule(schedule);
        return dto;
    }

    // ─── User Operations ──────────────────────────────────────────────────────

    // ✅ APPLY FOR LOAN
    @Transactional
    public LoanResponseDTO applyLoan(LoanApplicationRequestDTO request) {

        User user = getLoggedInUser();

        Account disbursementAccount = accountRepository.findById(request.getDisbursementAccountId())
                .orElseThrow(() -> new AccountNotFoundException("Disbursement account not found"));

        // Ownership check — account must belong to the applicant
        if (!disbursementAccount.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized: Disbursement account does not belong to you");
        }

        // Calculate EMI and totals at application time for display
        double emi = calculateEmi(request.getLoanAmount(), defaultLoanInterestRate, request.getTenureMonths());
        double totalPayable = Math.round(emi * request.getTenureMonths() * 100.0) / 100.0;
        double totalInterest = Math.round((totalPayable - request.getLoanAmount()) * 100.0) / 100.0;

        Loan loan = new Loan();
        loan.setUser(user);
        loan.setDisbursementAccount(disbursementAccount);
        loan.setLoanAmount(request.getLoanAmount());
        loan.setAnnualInterestRate(defaultLoanInterestRate);
        loan.setTenureMonths(request.getTenureMonths());
        loan.setEmiAmount(emi);
        loan.setTotalPayable(totalPayable);
        loan.setTotalInterest(totalInterest);
        loan.setStatus(LoanStatus.PENDING);

        Loan saved = loanRepository.save(loan);

        // 📧 Notify user
        emailService.sendLoanApplicationEmail(
                user.getEmail(), user.getName(),
                request.getLoanAmount(), request.getTenureMonths()
        );

        return toDTO(saved);
    }

    // ✅ GET ALL LOANS FOR LOGGED-IN USER
    public List<LoanResponseDTO> getMyLoans() {
        User user = getLoggedInUser();
        return loanRepository.findByUser(user)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    // ✅ GET A SINGLE LOAN BY ID (with ownership check)
    public LoanResponseDTO getLoanById(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new LoanNotFoundException("Loan not found with id: " + loanId));

        User loggedIn = getLoggedInUser();

        // Users can only see their own loans; admins can see all
        boolean isAdmin = loggedIn.getRole() == Role.ROLE_ADMIN;
        if (!isAdmin && !loan.getUser().getId().equals(loggedIn.getId())) {
            throw new RuntimeException("Unauthorized: You can only view your own loans");
        }

        return toDTO(loan);
    }

    // ✅ PAY EMI — deducted from disbursement account, PIN required
    @Transactional
    public LoanRepaymentResponseDTO payEmi(Long loanId, LoanRepaymentRequestDTO request) {

        User user = getLoggedInUser();

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new LoanNotFoundException("Loan not found"));

        // Ownership check
        if (!loan.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized: This loan does not belong to you");
        }

        if (loan.getStatus() != LoanStatus.ACTIVE) {
            throw new RuntimeException("Cannot pay EMI — loan status is: " + loan.getStatus());
        }

        if (loan.getEmisPaid() >= loan.getTenureMonths()) {
            throw new RuntimeException("All EMIs already paid. Loan is closed.");
        }

        Account account = loan.getDisbursementAccount();

        // 🔐 Validate PIN before deducting money
        accountService.validateTransactionPin(account, request.getPin());

        if (account.getBalance() < loan.getEmiAmount()) {
            throw new RuntimeException(
                "Insufficient balance to pay EMI of ₹" + loan.getEmiAmount() +
                ". Current balance: ₹" + account.getBalance());
        }

        // Deduct EMI from account
        account.setBalance(account.getBalance() - loan.getEmiAmount());
        accountRepository.save(account);

        // Record EMI repayment
        int nextEmiNumber = loan.getEmisPaid() + 1;
        LoanRepayment repayment = new LoanRepayment();
        repayment.setLoan(loan);
        repayment.setEmiNumber(nextEmiNumber);
        repayment.setAmount(loan.getEmiAmount());
        loanRepaymentRepository.save(repayment);

        // Record as a transaction in account history
        Transaction emiTx = new Transaction();
        emiTx.setType(TransactionType.EMI_PAYMENT);
        emiTx.setAmount(loan.getEmiAmount());
        emiTx.setAccount(account);
        transactionRepository.save(emiTx);

        // Update loan EMI count
        loan.setEmisPaid(nextEmiNumber);

        // If all EMIs paid, close the loan
        if (nextEmiNumber >= loan.getTenureMonths()) {
            loan.setStatus(LoanStatus.CLOSED);
        }

        loanRepository.save(loan);

        // 📧 Notify user
        emailService.sendEmiPaymentEmail(
                user.getEmail(), user.getName(),
                loanId, nextEmiNumber, loan.getTenureMonths(),
                loan.getEmiAmount(), account.getBalance()
        );

        LoanRepaymentResponseDTO response = new LoanRepaymentResponseDTO();
        response.setRepaymentId(repayment.getId());
        response.setLoanId(loanId);
        response.setEmiNumber(nextEmiNumber);
        response.setAmount(loan.getEmiAmount());
        response.setPaidAt(repayment.getPaidAt());
        response.setEmisPaid(nextEmiNumber);
        response.setEmisRemaining(loan.getTenureMonths() - nextEmiNumber);
        response.setLoanStatus(loan.getStatus().name());
        response.setAccountBalanceAfterPayment(account.getBalance());

        return response;
    }

    // ─── Admin Operations ─────────────────────────────────────────────────────

    // ✅ GET ALL PENDING LOANS — admin review queue
    public List<LoanResponseDTO> getPendingLoans() {
        return loanRepository.findByStatus(LoanStatus.PENDING)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    // ✅ GET ALL LOANS — admin view
    public List<LoanResponseDTO> getAllLoans() {
        return loanRepository.findAll()
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    // ✅ APPROVE LOAN — disburse money to account
    @Transactional
    public LoanResponseDTO approveLoan(Long loanId) {

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new LoanNotFoundException("Loan not found"));

        if (loan.getStatus() != LoanStatus.PENDING) {
            throw new RuntimeException("Loan is not in PENDING status. Current: " + loan.getStatus());
        }

        // Credit loan amount to disbursement account
        Account account = loan.getDisbursementAccount();
        account.setBalance(account.getBalance() + loan.getLoanAmount());
        accountRepository.save(account);

        // Record as transaction in account history
        Transaction disburseTx = new Transaction();
        disburseTx.setType(TransactionType.LOAN_DISBURSEMENT);
        disburseTx.setAmount(loan.getLoanAmount());
        disburseTx.setAccount(account);
        transactionRepository.save(disburseTx);

        // Activate loan
        loan.setStatus(LoanStatus.ACTIVE);
        loan.setStartDate(LocalDate.now());
        loanRepository.save(loan);

        // 📧 Notify user with EMI details
        emailService.sendLoanApprovedEmail(
                loan.getUser().getEmail(),
                loan.getUser().getName(),
                loan.getLoanAmount(),
                loan.getTenureMonths(),
                loan.getEmiAmount(),
                loan.getTotalPayable(),
                account.getAccountNumber()
        );

        return toDTO(loan);
    }

    // ✅ REJECT LOAN
    @Transactional
    public LoanResponseDTO rejectLoan(Long loanId, String reason) {

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new LoanNotFoundException("Loan not found"));

        if (loan.getStatus() != LoanStatus.PENDING) {
            throw new RuntimeException("Loan is not in PENDING status. Current: " + loan.getStatus());
        }

        loan.setStatus(LoanStatus.REJECTED);
        loan.setRejectionReason(reason);
        loanRepository.save(loan);

        // 📧 Notify user
        emailService.sendLoanRejectedEmail(
                loan.getUser().getEmail(),
                loan.getUser().getName(),
                loan.getLoanAmount(),
                reason
        );

        return toDTO(loan);
    }
}