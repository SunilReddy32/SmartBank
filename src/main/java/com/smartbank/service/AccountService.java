package com.smartbank.service;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import com.smartbank.repository.AccountRepository;
import com.smartbank.repository.UserRepository;
import com.smartbank.dto.AccountResponseDTO;
import com.smartbank.entity.Account;
import com.smartbank.entity.User;
import com.smartbank.exception.AccountNotFoundException;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    public AccountResponseDTO createAccount(Long userId, Account account) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        account.setUser(user);
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
            .orElseThrow(() -> new RuntimeException("Account not found"));

    return account.getBalance();
}

public AccountResponseDTO getAccountDetails(Long accountId) {

    Account account = accountRepository.findById(accountId)
            .orElseThrow(() -> new AccountNotFoundException("Account not found"));

    AccountResponseDTO response = new AccountResponseDTO();

    response.setId(account.getId());
    response.setBalance(account.getBalance());
    response.setUserId(account.getUser().getId());

    return response;
}
}