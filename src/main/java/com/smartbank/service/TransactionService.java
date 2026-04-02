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

    // 🔐 Get logged-in user
    private User getLoggedInUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // 🔐 Validate account belongs to logged-in user
    private void validateAccountOwnership(Account account) {
        User user = getLoggedInUser();
        if (!account.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access to this account");
        }
    }

    // 🔧 Map Transaction → DTO
    private TransactionResponseDTO toDTO(Transaction tx) {
        TransactionResponseDTO dto = new TransactionResponseDTO();
        dto.setTransactionId(tx.getId());
        dto.setType(tx.getType().name());
        dto.setAmount(tx.getAmount());
        dto.setAccountId(tx.getAccount().getId());
        dto.setCreatedAt(tx.getCreatedAt());
        return dto;
    }

    // ✅ DEPOSIT
    @Transactional
    public TransactionResponseDTO deposit(Long accountId, double amount) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));
        validateAccountOwnership(account);

        account.setBalance(account.getBalance() + amount);
        accountRepository.save(account);

        Transaction tx = new Transaction();
        tx.setType(TransactionType.DEPOSIT);
        tx.setAmount(amount);
        tx.setAccount(account);

        return toDTO(transactionRepository.save(tx));
    }

    // ✅ WITHDRAW
    @Transactional
    public TransactionResponseDTO withdraw(Long accountId, double amount) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));
        validateAccountOwnership(account);

        if (account.getBalance() < amount) {
            throw new InsufficientBalanceException("Insufficient balance");
        }

        account.setBalance(account.getBalance() - amount);
        accountRepository.save(account);

        Transaction tx = new Transaction();
        tx.setType(TransactionType.WITHDRAW);
        tx.setAmount(amount);
        tx.setAccount(account);

        return toDTO(transactionRepository.save(tx));
    }

    // ✅ TRANSFER BY ACCOUNT ID (original — kept for backward compatibility)
    @Transactional
    public TransactionResponseDTO transfer(Long fromAccountId, Long toAccountId, double amount) {
        Account fromAccount = accountRepository.findById(fromAccountId)
                .orElseThrow(() -> new AccountNotFoundException("Sender account not found"));
        Account toAccount = accountRepository.findById(toAccountId)
                .orElseThrow(() -> new AccountNotFoundException("Receiver account not found"));

        return executeTransfer(fromAccount, toAccount, amount);
    }

    // ✅ NEW: TRANSFER BY ACCOUNT NUMBER (real-world banking style)
    // Users enter the visible 10-digit account number, not an internal DB id
    @Transactional
    public TransactionResponseDTO transferByAccountNumber(
            String fromAccountNumber, String toAccountNumber, double amount) {

        Account fromAccount = accountRepository.findByAccountNumber(fromAccountNumber)
                .orElseThrow(() -> new AccountNotFoundException(
                        "Sender account not found: " + fromAccountNumber));

        Account toAccount = accountRepository.findByAccountNumber(toAccountNumber)
                .orElseThrow(() -> new AccountNotFoundException(
                        "Receiver account not found: " + toAccountNumber));

        return executeTransfer(fromAccount, toAccount, amount);
    }

    // 🔧 Shared transfer logic — avoids duplication between both transfer methods
    private TransactionResponseDTO executeTransfer(Account fromAccount, Account toAccount, double amount) {
        validateAccountOwnership(fromAccount);

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

        // 📤 Debit record — appears in sender's history
        Transaction debitTx = new Transaction();
        debitTx.setType(TransactionType.TRANSFER);
        debitTx.setAmount(amount);
        debitTx.setAccount(fromAccount);
        Transaction savedDebit = transactionRepository.save(debitTx);

        // 📥 Credit record — appears in receiver's history
        Transaction creditTx = new Transaction();
        creditTx.setType(TransactionType.TRANSFER);
        creditTx.setAmount(amount);
        creditTx.setAccount(toAccount);
        transactionRepository.save(creditTx);

        return toDTO(savedDebit);
    }

    // ✅ GET TRANSACTIONS WITH FILTER + PAGINATION
    public List<TransactionResponseDTO> getTransactions(Long accountId, String type, Pageable pageable) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));
        validateAccountOwnership(account);

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