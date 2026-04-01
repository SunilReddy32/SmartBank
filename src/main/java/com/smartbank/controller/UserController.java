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

    @PutMapping("/{userId}")
    public UserResponseDTO updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserRequestDTO request) {

        return userService.updateUser(userId, request);
    }
}
