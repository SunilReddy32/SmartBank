package com.smartbank.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

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

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final EmailService emailService;

    private User getLoggedInUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Logged-in user not found"));
    }

    // ✅ REGISTER + welcome email
    public UserResponseDTO register(RegisterRequestDTO request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException("Email already exists");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        UserResponseDTO response = toDTO(userRepository.save(user));

        // 📧 Send welcome email after successful registration
        emailService.sendWelcomeEmail(user.getEmail(), user.getName());

        return response;
    }

    // ✅ LOGIN + login alert email
    public LoginResponseDTO login(LoginRequestDTO request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        String token = JwtUtil.generateToken(user.getEmail(), user.getRole().name());

        LoginResponseDTO response = new LoginResponseDTO();
        response.setMessage("Login successful");
        response.setEmail(user.getEmail());
        response.setToken(token);
        response.setRole(user.getRole().name());

        // 📧 Send login notification email
        emailService.sendLoginNotification(user.getEmail(), user.getName());

        return response;
    }

    // ✅ GET USER PROFILE
    public UserResponseDTO getUser(Long userId) {
        User loggedIn = getLoggedInUser();
        if (!loggedIn.getId().equals(userId)) {
            throw new RuntimeException("Unauthorized: You can only view your own profile");
        }
        return toDTO(userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found")));
    }

    // ✅ UPDATE USER
    @Transactional
    public UserResponseDTO updateUser(Long userId, UpdateUserRequestDTO request) {

        User loggedIn = getLoggedInUser();
        if (!loggedIn.getId().equals(userId)) {
            throw new RuntimeException("Unauthorized: You can only update your own profile");
        }

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
        response.setRole(user.getRole().name());
        return response;
    }
}