package com.smartbank.service;

import org.springframework.stereotype.Service;
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

    // 🔐 Common method to get logged-in user
    private User getLoggedInUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // 🔐 Validate account ownership
    private void validateAccountOwnership(Account account) {
        User user = getLoggedInUser();

        if (!account.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access to this account");
        }
    }

    // ✅ DEPOSIT
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

        Transaction savedTransaction = transactionRepository.save(transaction);

        TransactionResponseDTO response = new TransactionResponseDTO();
        response.setTransactionId(savedTransaction.getId());
        response.setType(savedTransaction.getType().name());
        response.setAmount(savedTransaction.getAmount());
        response.setAccountId(account.getId());

        return response;
    }

    // ✅ WITHDRAW
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

        Transaction savedTransaction = transactionRepository.save(transaction);

        TransactionResponseDTO response = new TransactionResponseDTO();
        response.setTransactionId(savedTransaction.getId());
        response.setType(savedTransaction.getType().name()); 
        response.setAmount(savedTransaction.getAmount());
        response.setAccountId(account.getId());

        return response;
    }

    // ✅ TRANSFER
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

        Transaction transaction = new Transaction();
        transaction.setType(TransactionType.TRANSFER);
        transaction.setAmount(amount);
        transaction.setAccount(fromAccount);

        Transaction savedTransaction = transactionRepository.save(transaction);

        TransactionResponseDTO response = new TransactionResponseDTO();
        response.setTransactionId(savedTransaction.getId());
        response.setType(savedTransaction.getType().name());
        response.setAmount(savedTransaction.getAmount());
        response.setAccountId(fromAccount.getId());

        return response;
    }

    // ✅ GET TRANSACTIONS WITH FILTER + PAGINATION
    public List<TransactionResponseDTO> getTransactions(Long accountId, String type, Pageable pageable) {

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));

        validateAccountOwnership(account);

        Page<Transaction> transactionPage;

        if (type != null) {
            transactionPage = transactionRepository.findByAccountAndType(account, type, pageable);
        } else {
            transactionPage = transactionRepository.findByAccount(account, pageable);
        }

        List<TransactionResponseDTO> responseList = new ArrayList<>();

        for (Transaction tx : transactionPage.getContent()) {

            TransactionResponseDTO dto = new TransactionResponseDTO();
            dto.setTransactionId(tx.getId());
            dto.setType(tx.getType().name());
            dto.setAmount(tx.getAmount());
            dto.setAccountId(accountId);

            responseList.add(dto);
        }

        return responseList;
    }
}