package com.smartbank.service;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.smartbank.repository.UserRepository;
import com.smartbank.repository.AccountRepository;
import com.smartbank.repository.TransactionRepository;
import com.smartbank.entity.User;
import com.smartbank.entity.Account;
import com.smartbank.entity.Transaction;
import com.smartbank.exception.UserNotFoundException;
import com.smartbank.dto.DashboardResponseDTO;
import com.smartbank.dto.AccountResponseDTO;
import com.smartbank.dto.TransactionResponseDTO;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    // 🔐 Get logged-in user
    private User getLoggedInUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Logged-in user not found"));
    }

    // ✅ GET DASHBOARD
    // Single endpoint the frontend calls on page load — returns everything at once
    public DashboardResponseDTO getDashboard(Long userId) {

        // 🔐 Ownership check
        User loggedIn = getLoggedInUser();
        if (!loggedIn.getId().equals(userId)) {
            throw new RuntimeException("Unauthorized: You can only view your own dashboard");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // All accounts belonging to this user
        List<Account> accounts = accountRepository.findByUser(user);

        // Total balance = sum across all accounts
        double totalBalance = accounts.stream()
                .mapToDouble(Account::getBalance)
                .sum();

        // Last 5 transactions across ALL accounts, newest first
        List<Transaction> recentTxns = transactionRepository
                .findByAccountInOrderByCreatedAtDesc(accounts, PageRequest.of(0, 5));

        DashboardResponseDTO dashboard = new DashboardResponseDTO();
        dashboard.setUserId(user.getId());
        dashboard.setUserName(user.getName());
        dashboard.setTotalAccounts(accounts.size());
        dashboard.setTotalBalance(totalBalance);
        dashboard.setAccounts(accounts.stream().map(this::toAccountDTO).collect(Collectors.toList()));
        dashboard.setRecentTransactions(recentTxns.stream().map(this::toTxnDTO).collect(Collectors.toList()));

        return dashboard;
    }

    private AccountResponseDTO toAccountDTO(Account a) {
        AccountResponseDTO dto = new AccountResponseDTO();
        dto.setId(a.getId());
        dto.setAccountNumber(a.getAccountNumber());
        dto.setBalance(a.getBalance());
        dto.setUserId(a.getUser().getId());
        return dto;
    }

    private TransactionResponseDTO toTxnDTO(Transaction tx) {
        TransactionResponseDTO dto = new TransactionResponseDTO();
        dto.setTransactionId(tx.getId());
        dto.setType(tx.getType().name());
        dto.setAmount(tx.getAmount());
        dto.setAccountId(tx.getAccount().getId());
        dto.setCreatedAt(tx.getCreatedAt());
        return dto;
    }
}