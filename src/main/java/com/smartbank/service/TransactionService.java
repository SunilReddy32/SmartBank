package com.smartbank.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.smartbank.repository.UserRepository;
import com.smartbank.entity.User;

import com.smartbank.repository.TransactionRepository;
import com.smartbank.repository.AccountRepository;
import com.smartbank.dto.TransactionResponseDTO;
import com.smartbank.entity.Transaction;
import com.smartbank.entity.TransactionType;
import com.smartbank.entity.Account;

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

    private User getLoggedInUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private void validateAccountOwnership(Account account) {
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
        dto.setCreatedAt(tx.getCreatedAt());   // ✅ NEW: timestamp
        return dto;
    }

    @Transactional
    public TransactionResponseDTO deposit(Long accountId, double amount) {

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));

        validateAccountOwnership(account);

        account.setBalance(account.getBalance() + amount);
        accountRepository.save(account);

        Transaction transaction = new Transaction();
        transaction.setType(TransactionType.DEPOSIT);
        transaction.setAmount(amount);
        transaction.setAccount(account);

        return toDTO(transactionRepository.save(transaction));
    }

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

        Transaction transaction = new Transaction();
        transaction.setType(TransactionType.WITHDRAW);
        transaction.setAmount(amount);
        transaction.setAccount(account);

        return toDTO(transactionRepository.save(transaction));
    }

    // ✅ @Transactional + dual recording (from previous fix)
    @Transactional
    public TransactionResponseDTO transfer(Long fromAccountId, Long toAccountId, double amount) {

        Account fromAccount = accountRepository.findById(fromAccountId)
                .orElseThrow(() -> new AccountNotFoundException("Sender account not found"));

        Account toAccount = accountRepository.findById(toAccountId)
                .orElseThrow(() -> new AccountNotFoundException("Receiver account not found"));

        validateAccountOwnership(fromAccount);

        if (fromAccount.getBalance() < amount) {
            throw new InsufficientBalanceException("Insufficient balance");
        }

        fromAccount.setBalance(fromAccount.getBalance() - amount);
        toAccount.setBalance(toAccount.getBalance() + amount);

        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        // 📤 Sender record
        Transaction debitTx = new Transaction();
        debitTx.setType(TransactionType.TRANSFER);
        debitTx.setAmount(amount);
        debitTx.setAccount(fromAccount);
        Transaction savedDebitTx = transactionRepository.save(debitTx);

        // 📥 Receiver record
        Transaction creditTx = new Transaction();
        creditTx.setType(TransactionType.TRANSFER);
        creditTx.setAmount(amount);
        creditTx.setAccount(toAccount);
        transactionRepository.save(creditTx);

        return toDTO(savedDebitTx);
    }

    public List<TransactionResponseDTO> getTransactions(Long accountId, String type, Pageable pageable) {

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));

        validateAccountOwnership(account);

        Page<Transaction> transactionPage = (type != null)
                ? transactionRepository.findByAccountAndType(account, type, pageable)
                : transactionRepository.findByAccount(account, pageable);

        List<TransactionResponseDTO> responseList = new ArrayList<>();
        for (Transaction tx : transactionPage.getContent()) {
            responseList.add(toDTO(tx));
        }

        return responseList;
    }
}