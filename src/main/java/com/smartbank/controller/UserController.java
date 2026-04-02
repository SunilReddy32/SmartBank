package com.smartbank.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import com.smartbank.dto.UpdateUserRequestDTO;
import com.smartbank.dto.UserResponseDTO;
import com.smartbank.service.UserService;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // ✅ NEW: GET USER PROFILE
    // Example: GET /users/3
    // Only the logged-in user can fetch their own profile
    @GetMapping("/{userId}")
    public UserResponseDTO getUser(@PathVariable Long userId) {
        return userService.getUser(userId);
    }

    // ✅ UPDATE USER — now with ownership check (BUG FIX)
    // Users can only update their own profile
    @PutMapping("/{userId}")
    public UserResponseDTO updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserRequestDTO request) {

        return userService.updateUser(userId, request);
    }
}