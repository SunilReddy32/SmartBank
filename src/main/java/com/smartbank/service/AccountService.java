package com.smartbank.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.smartbank.repository.AccountRepository;
import com.smartbank.repository.UserRepository;
import com.smartbank.dto.AccountResponseDTO;
import com.smartbank.dto.CreateAccountRequestDTO;
import com.smartbank.entity.Account;
import com.smartbank.entity.User;
import com.smartbank.exception.AccountNotFoundException;
import com.smartbank.exception.UserNotFoundException;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    // 🔐 Helper: get logged-in user from JWT
    private User getLoggedInUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Logged-in user not found"));
    }

    // ✅ CREATE ACCOUNT
    // BUG FIX: Now accepts a DTO instead of a raw entity
    // NEW: Account number is auto-generated server-side — client never sends it
    @Transactional
    public AccountResponseDTO createAccount(Long userId, CreateAccountRequestDTO request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // 🔐 Ownership check — users can only create accounts for themselves
        User loggedIn = getLoggedInUser();
        if (!loggedIn.getId().equals(userId)) {
            throw new RuntimeException("Unauthorized: You can only create accounts for yourself");
        }

        Account account = new Account();
        account.setUser(user);
        account.setBalance(request.getInitialBalance());

        // ✅ NEW: Auto-generate a unique 10-digit account number
        account.setAccountNumber(generateUniqueAccountNumber());

        Account savedAccount = accountRepository.save(account);

        return toDTO(savedAccount);
    }

    // ✅ GET BALANCE
    public double getBalance(Long accountId) {

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));

        // 🔐 Ownership check
        User loggedIn = getLoggedInUser();
        if (!account.getUser().getId().equals(loggedIn.getId())) {
            throw new RuntimeException("Unauthorized: This account does not belong to you");
        }

        return account.getBalance();
    }

    // ✅ GET ACCOUNT DETAILS
    public AccountResponseDTO getAccountDetails(Long accountId) {

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));

        // 🔐 Ownership check
        User loggedIn = getLoggedInUser();
        if (!account.getUser().getId().equals(loggedIn.getId())) {
            throw new RuntimeException("Unauthorized: This account does not belong to you");
        }

        return toDTO(account);
    }

    // ✅ NEW: GET ALL ACCOUNTS FOR A USER
    public List<AccountResponseDTO> getAccountsByUser(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // 🔐 Ownership check — only the logged-in user can list their own accounts
        User loggedIn = getLoggedInUser();
        if (!loggedIn.getId().equals(userId)) {
            throw new RuntimeException("Unauthorized: You can only view your own accounts");
        }

        return accountRepository.findByUser(user)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // 🔧 Auto-generate a unique 10-digit account number
    private String generateUniqueAccountNumber() {
        Random random = new Random();
        String accountNumber;

        do {
            // Generate a random 10-digit number (starts from 1000000000 to avoid leading zeros)
            long number = 1000000000L + (long) (random.nextDouble() * 9000000000L);
            accountNumber = String.valueOf(number);
        } while (accountRepository.existsByAccountNumber(accountNumber)); // retry if collision

        return accountNumber;
    }

    // 🔧 Helper: convert Account entity → AccountResponseDTO
    private AccountResponseDTO toDTO(Account account) {
        AccountResponseDTO response = new AccountResponseDTO();
        response.setId(account.getId());
        response.setAccountNumber(account.getAccountNumber());
        response.setBalance(account.getBalance());
        response.setUserId(account.getUser().getId());
        return response;
    }
}