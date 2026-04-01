package com.smartbank.controller;

import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import com.smartbank.service.UserService;

import jakarta.validation.Valid;

import com.smartbank.dto.UserResponseDTO;
import com.smartbank.dto.LoginRequestDTO;
import com.smartbank.dto.LoginResponseDTO;
import com.smartbank.dto.RegisterRequestDTO;


@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    
   @PostMapping("/register")
public UserResponseDTO register(@Valid @RequestBody RegisterRequestDTO request) {
    return userService.register(request);
}

    @PostMapping("/login")
public LoginResponseDTO login(@Valid @RequestBody LoginRequestDTO request) {
    return userService.login(request);
}

}
