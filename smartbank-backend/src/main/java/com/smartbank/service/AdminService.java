package com.smartbank.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.smartbank.dto.AccountResponseDTO;
import com.smartbank.dto.UserResponseDTO;
import com.smartbank.entity.Account;
import com.smartbank.entity.Role;
import com.smartbank.entity.User;
import com.smartbank.exception.UserNotFoundException;
import com.smartbank.repository.AccountRepository;
import com.smartbank.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;

    // ── All users ────────────────────────────────────────────────────────────
    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::toUserDTO)
                .toList();
    }

    public UserResponseDTO getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        return toUserDTO(user);
    }

    // ── All accounts ─────────────────────────────────────────────────────────
    public List<AccountResponseDTO> getAllAccounts() {
        return accountRepository.findAll()
                .stream()
                .map(this::toAccountDTO)
                .toList();
    }

    public List<AccountResponseDTO> getAccountsByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return accountRepository.findByUser(user)
                .stream()
                .map(this::toAccountDTO)
                .toList();
    }

    // ── Role management ──────────────────────────────────────────────────────
    public UserResponseDTO updateUserRole(Long userId, String roleName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        try {
            user.setRole(Role.valueOf(roleName));
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid role: " + roleName + ". Use ROLE_USER or ROLE_ADMIN");
        }

        return toUserDTO(userRepository.save(user));
    }

    // ── Mappers ──────────────────────────────────────────────────────────────
    private UserResponseDTO toUserDTO(User user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole().name());
        return dto;
    }

    private AccountResponseDTO toAccountDTO(Account account) {
        AccountResponseDTO dto = new AccountResponseDTO();
        dto.setId(account.getId());
        dto.setAccountNumber(account.getAccountNumber());
        dto.setBalance(account.getBalance());
        dto.setUserId(account.getUser().getId());
        return dto;
    }
}