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

    // 🔐 Helper: get the currently logged-in user from JWT
    private User getLoggedInUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Logged-in user not found"));
    }

    // ✅ REGISTER
    public UserResponseDTO register(RegisterRequestDTO request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException("Email already exists");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        // role defaults to ROLE_USER via entity field initializer

        return toDTO(userRepository.save(user));
    }

    // ✅ LOGIN
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
        response.setRole(user.getRole().name()); // ✅ BUG FIX: was never set before

        return response;
    }

    // ✅ GET USER PROFILE — only logged-in user can fetch their own profile
    public UserResponseDTO getUser(Long userId) {

        User loggedIn = getLoggedInUser();

        if (!loggedIn.getId().equals(userId)) {
            throw new RuntimeException("Unauthorized: You can only view your own profile");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return toDTO(user);
    }

    // ✅ UPDATE USER — with ownership check
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

    // 🔧 Helper: map User → UserResponseDTO
    private UserResponseDTO toDTO(User user) {
        UserResponseDTO response = new UserResponseDTO();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole().name()); // ✅ BUG FIX: was never set before
        return response;
    }
}