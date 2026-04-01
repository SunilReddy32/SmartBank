package com.smartbank.service;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import com.smartbank.repository.UserRepository;
import com.smartbank.security.JwtUtil;
import com.smartbank.entity.User;
import com.smartbank.exception.DuplicateEmailException;
import com.smartbank.exception.UserNotFoundException;
import com.smartbank.dto.LoginRequestDTO;
import com.smartbank.dto.LoginResponseDTO;
import com.smartbank.dto.RegisterRequestDTO;
import com.smartbank.dto.UpdateUserRequestDTO;
import com.smartbank.dto.UserResponseDTO;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserResponseDTO register(RegisterRequestDTO  request) {

        if (userRepository.existsByEmail(request.getEmail())) {
        throw new DuplicateEmailException("Email already exists");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        User savedUser = userRepository.save(user);

        UserResponseDTO response = new UserResponseDTO();
        response.setId(savedUser.getId());
        response.setName(savedUser.getName());
        response.setEmail(savedUser.getEmail());

        return response;
    }

    
    public LoginResponseDTO login(LoginRequestDTO request) {

    User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new RuntimeException("User not found"));

    if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
        throw new RuntimeException("Invalid password");
    }

    String token = JwtUtil.generateToken(user.getEmail());

    LoginResponseDTO response = new LoginResponseDTO();
    response.setMessage("Login successful");
    response.setEmail(user.getEmail());
    response.setToken(token);


    return response;
}

public UserResponseDTO updateUser(Long userId, UpdateUserRequestDTO request) {

    User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("User not found"));

    // Update name
    if (request.getName() != null && !request.getName().isBlank()) {
        user.setName(request.getName());
    }

    // Update email (with duplicate check)
    if (request.getEmail() != null && !request.getEmail().isBlank()) {

        if (userRepository.existsByEmail(request.getEmail())
                && !user.getEmail().equals(request.getEmail())) {

            throw new DuplicateEmailException("Email already exists");
        }

        user.setEmail(request.getEmail());
    }

    // Update password (with encryption)
    if (request.getPassword() != null && !request.getPassword().isBlank()) {
        user.setPassword(passwordEncoder.encode(request.getPassword()));
    }

    User updatedUser = userRepository.save(user);

    UserResponseDTO response = new UserResponseDTO();
    response.setId(updatedUser.getId());
    response.setName(updatedUser.getName());
    response.setEmail(updatedUser.getEmail());

    return response;
}
}
