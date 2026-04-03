package com.smartbank.service;

import com.smartbank.dto.AccountResponseDTO;
import com.smartbank.dto.CreateAccountRequestDTO;
import com.smartbank.dto.SetDailyLimitRequestDTO;
import com.smartbank.dto.SetPinRequestDTO;
import com.smartbank.entity.Account;
import com.smartbank.entity.User;
import com.smartbank.exception.AccountNotFoundException;
import com.smartbank.exception.UserNotFoundException;
import com.smartbank.repository.AccountRepository;
import com.smartbank.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    private User getLoggedInUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Logged-in user not found"));
    }

    private void validateOwnership(Account account) {
        User loggedIn = getLoggedInUser();
        if (!account.getUser().getId().equals(loggedIn.getId())) {
            throw new RuntimeException("Unauthorized: This account does not belong to you");
        }
    }

    @Transactional
    public AccountResponseDTO createAccount(Long userId, CreateAccountRequestDTO request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        User loggedIn = getLoggedInUser();
        if (!loggedIn.getId().equals(userId)) {
            throw new RuntimeException("Unauthorized: You can only create accounts for yourself");
        }
        Account account = new Account();
        account.setUser(user);
        account.setBalance(request.getInitialBalance());
        account.setAccountType(request.getAccountType());
        account.setAccountNumber(generateUniqueAccountNumber());
        return toDTO(accountRepository.save(account));
    }

    public double getBalance(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));
        validateOwnership(account);
        return account.getBalance();
    }

    public AccountResponseDTO getAccountDetails(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));
        validateOwnership(account);
        return toDTO(account);
    }

    public List<AccountResponseDTO> getAccountsByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        User loggedIn = getLoggedInUser();
        if (!loggedIn.getId().equals(userId)) {
            throw new RuntimeException("Unauthorized: You can only view your own accounts");
        }
        return accountRepository.findByUser(user).stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional
    public String setTransactionPin(Long accountId, SetPinRequestDTO request) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));
        validateOwnership(account);
        if (!request.getPin().equals(request.getConfirmPin())) {
            throw new RuntimeException("PINs do not match");
        }
        account.setTransactionPin(passwordEncoder.encode(request.getPin()));
        accountRepository.save(account);
        return "Transaction PIN set successfully for account " + account.getAccountNumber();
    }

    public void validateTransactionPin(Account account, String rawPin) {
        if (account.getTransactionPin() == null) {
            throw new RuntimeException(
                "Transaction PIN not set. Please set a PIN at PUT /accounts/" +
                account.getId() + "/pin before transacting.");
        }
        if (rawPin == null || rawPin.isBlank()) {
            throw new RuntimeException("Transaction PIN is required");
        }
        if (!passwordEncoder.matches(rawPin, account.getTransactionPin())) {
            throw new RuntimeException("Invalid transaction PIN");
        }
    }

    // ✅ FEATURE: Set daily limits
    @Transactional
    public AccountResponseDTO setDailyLimits(Long accountId, SetDailyLimitRequestDTO request) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));
        validateOwnership(account);
        account.setDailyWithdrawalLimit(request.getDailyWithdrawalLimit());
        account.setDailyTransferLimit(request.getDailyTransferLimit());
        return toDTO(accountRepository.save(account));
    }

    private String generateUniqueAccountNumber() {
        Random random = new Random();
        String accountNumber;
        do {
            long number = 1000000000L + (long)(random.nextDouble() * 9000000000L);
            accountNumber = String.valueOf(number);
        } while (accountRepository.existsByAccountNumber(accountNumber));
        return accountNumber;
    }

    private AccountResponseDTO toDTO(Account account) {
        AccountResponseDTO response = new AccountResponseDTO();
        response.setId(account.getId());
        response.setAccountNumber(account.getAccountNumber());
        response.setBalance(account.getBalance());
        response.setUserId(account.getUser().getId());
        response.setAccountType(account.getAccountType().name());
        response.setPinSet(account.getTransactionPin() != null);
        response.setDailyWithdrawalLimit(account.getDailyWithdrawalLimit());
        response.setDailyTransferLimit(account.getDailyTransferLimit());
        return response;
    }
}