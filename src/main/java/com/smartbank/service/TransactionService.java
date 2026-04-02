package com.smartbank.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.smartbank.repository.UserRepository;
import com.smartbank.repository.TransactionRepository;
import com.smartbank.repository.AccountRepository;
import com.smartbank.dto.TransactionResponseDTO;
import com.smartbank.entity.Transaction;
import com.smartbank.entity.TransactionType;
import com.smartbank.entity.Account;
import com.smartbank.entity.User;
import com.smartbank.exception.AccountNotFoundException;
import com.smartbank.exception.InsufficientBalanceException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final AccountService accountService;   // for PIN validation
    private final EmailService emailService;        // for notifications

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

    private TransactionResponseDTO toDTO(Transaction tx) {
        TransactionResponseDTO dto = new TransactionResponseDTO();
        dto.setTransactionId(tx.getId());
        dto.setType(tx.getType().name());
        dto.setAmount(tx.getAmount());
        dto.setAccountId(tx.getAccount().getId());
        dto.setCreatedAt(tx.getCreatedAt());
        return dto;
    }

    // ✅ DEPOSIT — no PIN required for deposits (only debits need PIN)
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

        // 📧 Email notification
        emailService.sendDepositEmail(
                account.getUser().getEmail(),
                account.getUser().getName(),
                account.getAccountNumber(),
                amount,
                account.getBalance()
        );

        return toDTO(saved);
    }

    // ✅ WITHDRAW — PIN required
    @Transactional
    public TransactionResponseDTO withdraw(Long accountId, double amount, String pin) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));
        validateOwnership(account);

        // 🔐 Validate transaction PIN before proceeding
        accountService.validateTransactionPin(account, pin);

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

        // 📧 Email notification
        emailService.sendWithdrawalEmail(
                account.getUser().getEmail(),
                account.getUser().getName(),
                account.getAccountNumber(),
                amount,
                account.getBalance()
        );

        return toDTO(saved);
    }

    // ✅ TRANSFER BY ACCOUNT ID — PIN required
    @Transactional
    public TransactionResponseDTO transfer(Long fromAccountId, Long toAccountId,
                                            double amount, String pin) {
        Account fromAccount = accountRepository.findById(fromAccountId)
                .orElseThrow(() -> new AccountNotFoundException("Sender account not found"));
        Account toAccount = accountRepository.findById(toAccountId)
                .orElseThrow(() -> new AccountNotFoundException("Receiver account not found"));

        return executeTransfer(fromAccount, toAccount, amount, pin);
    }

    // ✅ TRANSFER BY ACCOUNT NUMBER — PIN required
    @Transactional
    public TransactionResponseDTO transferByAccountNumber(String fromAccountNumber,
                                                           String toAccountNumber,
                                                           double amount, String pin) {
        Account fromAccount = accountRepository.findByAccountNumber(fromAccountNumber)
                .orElseThrow(() -> new AccountNotFoundException(
                        "Sender account not found: " + fromAccountNumber));
        Account toAccount = accountRepository.findByAccountNumber(toAccountNumber)
                .orElseThrow(() -> new AccountNotFoundException(
                        "Receiver account not found: " + toAccountNumber));

        return executeTransfer(fromAccount, toAccount, amount, pin);
    }

    // 🔧 Shared transfer logic
    private TransactionResponseDTO executeTransfer(Account fromAccount, Account toAccount,
                                                    double amount, String pin) {
        validateOwnership(fromAccount);

        // 🔐 Validate PIN before any money moves
        accountService.validateTransactionPin(fromAccount, pin);

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

        // 📤 Debit record — sender's history
        Transaction debitTx = new Transaction();
        debitTx.setType(TransactionType.TRANSFER);
        debitTx.setAmount(amount);
        debitTx.setAccount(fromAccount);
        Transaction savedDebit = transactionRepository.save(debitTx);

        // 📥 Credit record — receiver's history
        Transaction creditTx = new Transaction();
        creditTx.setType(TransactionType.TRANSFER);
        creditTx.setAmount(amount);
        creditTx.setAccount(toAccount);
        transactionRepository.save(creditTx);

        // 📧 Email sender
        emailService.sendTransferSentEmail(
                fromAccount.getUser().getEmail(),
                fromAccount.getUser().getName(),
                fromAccount.getAccountNumber(),
                toAccount.getAccountNumber(),
                amount,
                fromAccount.getBalance()
        );

        // 📧 Email receiver
        emailService.sendTransferReceivedEmail(
                toAccount.getUser().getEmail(),
                toAccount.getUser().getName(),
                fromAccount.getAccountNumber(),
                toAccount.getAccountNumber(),
                amount,
                toAccount.getBalance()
        );

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