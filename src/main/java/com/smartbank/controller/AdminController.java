package com.smartbank.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.smartbank.service.AdminService;
import com.smartbank.dto.AccountResponseDTO;
import com.smartbank.dto.UserResponseDTO;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_ADMIN')")   // ✅ every method requires ROLE_ADMIN
public class AdminController {

    private final AdminService adminService;

    // GET /admin/users — list every registered user
    @GetMapping("/users")
    public List<UserResponseDTO> getAllUsers() {
        return adminService.getAllUsers();
    }

    // GET /admin/users/{userId} — view any user's profile
    @GetMapping("/users/{userId}")
    public UserResponseDTO getUserById(@PathVariable Long userId) {
        return adminService.getUserById(userId);
    }

    // GET /admin/accounts — list every account in the system
    @GetMapping("/accounts")
    public List<AccountResponseDTO> getAllAccounts() {
        return adminService.getAllAccounts();
    }

    // GET /admin/accounts/user/{userId} — list all accounts of a specific user
    @GetMapping("/accounts/user/{userId}")
    public List<AccountResponseDTO> getAccountsByUser(@PathVariable Long userId) {
        return adminService.getAccountsByUser(userId);
    }

    // PUT /admin/users/{userId}/role?role=ROLE_ADMIN — promote or demote a user
    @PutMapping("/users/{userId}/role")
    public UserResponseDTO updateUserRole(
            @PathVariable Long userId,
            @RequestParam String role) {
        return adminService.updateUserRole(userId, role);
    }
}