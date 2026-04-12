package com.smartbank.service;

import com.smartbank.dto.TransactionResponseDTO;
import com.smartbank.entity.*;
import com.smartbank.exception.AccountNotFoundException;
import com.smartbank.exception.InsufficientBalanceException;
import com.smartbank.repository.AccountRepository;
import com.smartbank.repository.TransactionRepository;
import com.smartbank.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final AccountService accountService;
    private final EmailService emailService;

    private User getLoggedInUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private void validateOwnership(Account account) {
        User user = getLoggedInUser();
        if (!account.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access to this account");
        }
    }

    // ✅ FEATURE: Check daily limit before debit operations
    private void checkDailyLimit(Account account, double amount, TransactionType type) {
        if (type != TransactionType.WITHDRAW && type != TransactionType.TRANSFER) return;

        // Get start of today (midnight)
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();

        double todayTotal = transactionRepository.sumAmountByAccountAndTypeAndCreatedAtAfter(
                account, type, startOfDay);

        double limit = (type == TransactionType.WITHDRAW)
                ? account.getDailyWithdrawalLimit()
                : account.getDailyTransferLimit();

        // 0 = no limit configured
        if (limit <= 0) return;

        if (todayTotal + amount > limit) {
            double remaining = limit - todayTotal;
            throw new RuntimeException(
                "Daily " + type.name().toLowerCase() + " limit of ₹" + limit + " exceeded. " +
                "Already transacted: ₹" + todayTotal + ". " +
                "Remaining today: ₹" + Math.max(0, remaining));
        }
    }

    private TransactionResponseDTO toDTO(Transaction tx) {
        TransactionResponseDTO dto = new TransactionResponseDTO();
        dto.setTransactionId(tx.getId());
        dto.setType(tx.getType().name());
        dto.setAmount(tx.getAmount());
        dto.setAccountId(tx.getAccount().getId());
        dto.setCreatedAt(tx.getCreatedAt());
        return dto;
    }

    // ✅ DEPOSIT — no PIN, no daily limit (deposits are always allowed)
    @Transactional
    public TransactionResponseDTO deposit(Long accountId, double amount) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));
        validateOwnership(account);

        account.setBalance(account.getBalance() + amount);
        accountRepository.save(account);

        Transaction tx = new Transaction();
        tx.setType(TransactionType.DEPOSIT);
        tx.setAmount(amount);
        tx.setAccount(account);
        Transaction saved = transactionRepository.save(tx);

        emailService.sendDepositEmail(account.getUser().getEmail(),
                account.getUser().getName(), account.getAccountNumber(),
                amount, account.getBalance());

        return toDTO(saved);
    }

    // ✅ WITHDRAW — PIN required + daily limit check
    @Transactional
    public TransactionResponseDTO withdraw(Long accountId, double amount, String pin) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));
        validateOwnership(account);

        // 🔐 PIN validation
        accountService.validateTransactionPin(account, pin);

        // 📊 Daily limit check
        checkDailyLimit(account, amount, TransactionType.WITHDRAW);

        if (account.getBalance() < amount) {
            throw new InsufficientBalanceException("Insufficient balance");
        }

        account.setBalance(account.getBalance() - amount);
        accountRepository.save(account);

        Transaction tx = new Transaction();
        tx.setType(TransactionType.WITHDRAW);
        tx.setAmount(amount);
        tx.setAccount(account);
        Transaction saved = transactionRepository.save(tx);

        emailService.sendWithdrawalEmail(account.getUser().getEmail(),
                account.getUser().getName(), account.getAccountNumber(),
                amount, account.getBalance());

        return toDTO(saved);
    }

    // ✅ TRANSFER BY ACCOUNT ID
    @Transactional
    public TransactionResponseDTO transfer(Long fromAccountId, Long toAccountId,
                                            double amount, String pin) {
        Account fromAccount = accountRepository.findById(fromAccountId)
                .orElseThrow(() -> new AccountNotFoundException("Sender account not found"));
        Account toAccount = accountRepository.findById(toAccountId)
                .orElseThrow(() -> new AccountNotFoundException("Receiver account not found"));
        return executeTransfer(fromAccount, toAccount, amount, pin);
    }

    // ✅ TRANSFER BY ACCOUNT NUMBER
    @Transactional
    public TransactionResponseDTO transferByAccountNumber(String fromAccountNumber,
                                                           String toAccountNumber,
                                                           double amount, String pin) {
        Account fromAccount = accountRepository.findByAccountNumber(fromAccountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Sender account not found: " + fromAccountNumber));
        Account toAccount = accountRepository.findByAccountNumber(toAccountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Receiver account not found: " + toAccountNumber));
        return executeTransfer(fromAccount, toAccount, amount, pin);
    }

    private TransactionResponseDTO executeTransfer(Account fromAccount, Account toAccount,
                                                    double amount, String pin) {
        validateOwnership(fromAccount);

        // 🔐 PIN validation
        accountService.validateTransactionPin(fromAccount, pin);

        // 📊 Daily limit check
        checkDailyLimit(fromAccount, amount, TransactionType.TRANSFER);

        if (fromAccount.getAccountNumber().equals(toAccount.getAccountNumber())) {
            throw new RuntimeException("Cannot transfer to the same account");
        }

        if (fromAccount.getBalance() < amount) {
            throw new InsufficientBalanceException("Insufficient balance");
        }

        fromAccount.setBalance(fromAccount.getBalance() - amount);
        toAccount.setBalance(toAccount.getBalance() + amount);
        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        Transaction debitTx = new Transaction();
        debitTx.setType(TransactionType.TRANSFER);
        debitTx.setAmount(amount);
        debitTx.setAccount(fromAccount);
        Transaction savedDebit = transactionRepository.save(debitTx);

        Transaction creditTx = new Transaction();
        creditTx.setType(TransactionType.TRANSFER);
        creditTx.setAmount(amount);
        creditTx.setAccount(toAccount);
        transactionRepository.save(creditTx);

        emailService.sendTransferSentEmail(fromAccount.getUser().getEmail(),
                fromAccount.getUser().getName(), fromAccount.getAccountNumber(),
                toAccount.getAccountNumber(), amount, fromAccount.getBalance());

        emailService.sendTransferReceivedEmail(toAccount.getUser().getEmail(),
                toAccount.getUser().getName(), fromAccount.getAccountNumber(),
                toAccount.getAccountNumber(), amount, toAccount.getBalance());

        return toDTO(savedDebit);
    }

    // ✅ GET TRANSACTIONS WITH FILTER + PAGINATION
    public List<TransactionResponseDTO> getTransactions(Long accountId, String type, Pageable pageable) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));
        validateOwnership(account);

        Page<Transaction> page = (type != null)
                ? transactionRepository.findByAccountAndType(account, type, pageable)
                : transactionRepository.findByAccount(account, pageable);

        List<TransactionResponseDTO> responseList = new ArrayList<>();
        for (Transaction tx : page.getContent()) {
            responseList.add(toDTO(tx));
        }
        return responseList;
    }
}