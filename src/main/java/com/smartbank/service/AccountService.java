package com.smartbank.service;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import com.smartbank.repository.AccountRepository;
import com.smartbank.repository.UserRepository;
import com.smartbank.dto.AccountResponseDTO;
import com.smartbank.dto.CreateAccountRequestDTO;
import com.smartbank.entity.Account;
import com.smartbank.entity.User;
import com.smartbank.exception.AccountNotFoundException;

import java.util.Random;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    // ✅ FIXED: accepts DTO, generates account number server-side
    public AccountResponseDTO createAccount(Long userId, CreateAccountRequestDTO request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Account account = new Account();
        account.setUser(user);
        account.setBalance(request.getInitialBalance());

        // ✅ Auto-generate unique 10-digit account number
        account.setAccountNumber(generateUniqueAccountNumber());

        Account savedAccount = accountRepository.save(account);

        AccountResponseDTO response = new AccountResponseDTO();
        response.setId(savedAccount.getId());
        response.setAccountNumber(savedAccount.getAccountNumber());
        response.setBalance(savedAccount.getBalance());
        response.setUserId(user.getId());

        return response;
    }

    public double getBalance(Long accountId) {

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));

        return account.getBalance();
    }

    public AccountResponseDTO getAccountDetails(Long accountId) {

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));

        AccountResponseDTO response = new AccountResponseDTO();
        response.setId(account.getId());
        response.setAccountNumber(account.getAccountNumber());
        response.setBalance(account.getBalance());
        response.setUserId(account.getUser().getId());

        return response;
    }

    // ✅ Generates a unique 10-digit account number, retries on collision
    private String generateUniqueAccountNumber() {
        Random random = new Random();
        String accountNumber;
        do {
            long number = 1000000000L + (long)(random.nextDouble() * 9000000000L);
            accountNumber = String.valueOf(number);
        } while (accountRepository.existsByAccountNumber(accountNumber));

        return accountNumber;
    }
}