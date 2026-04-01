package com.smartbank.service;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import com.smartbank.repository.UserRepository;
import com.smartbank.security.JwtUtil;
import com.smartbank.entity.User;
import com.smartbank.entity.Role;
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

    public UserResponseDTO register(RegisterRequestDTO request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException("Email already exists");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.ROLE_USER);   // ✅ always ROLE_USER on self-registration

        User savedUser = userRepository.save(user);
        return toDTO(savedUser);
    }

    public LoginResponseDTO login(LoginRequestDTO request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        // ✅ Role is now embedded in the JWT so Spring Security can enforce it
        String token = JwtUtil.generateToken(user.getEmail(), user.getRole().name());

        LoginResponseDTO response = new LoginResponseDTO();
        response.setMessage("Login successful");
        response.setEmail(user.getEmail());
        response.setToken(token);
        response.setRole(user.getRole().name());   // ✅ frontend can redirect to admin/user dashboard

        return response;
    }

    public UserResponseDTO updateUser(Long userId, UpdateUserRequestDTO request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (request.getName() != null && !request.getName().isBlank()) {
            user.setName(request.getName());
        }

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            if (userRepository.existsByEmail(request.getEmail())
                    && !user.getEmail().equals(request.getEmail())) {
                throw new DuplicateEmailException("Email already exists");
            }
            user.setEmail(request.getEmail());
        }

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        return toDTO(userRepository.save(user));
    }

    private UserResponseDTO toDTO(User user) {
        UserResponseDTO response = new UserResponseDTO();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole().name());   // ✅ expose role
        return response;
    }
}