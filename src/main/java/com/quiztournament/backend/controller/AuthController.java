package com.quiztournament.backend.controller;

import com.quiztournament.backend.dto.LoginRequest;
import com.quiztournament.backend.dto.RegisterRequest;
import com.quiztournament.backend.dto.UserResponse;
import com.quiztournament.backend.entity.Role;
import com.quiztournament.backend.entity.User;
import com.quiztournament.backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register/admin")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> registerAdmin(@Valid @RequestBody RegisterRequest request) {
        User user = authService.register(request, Role.ADMIN);
        return Map.of("message", "Admin registered successfully", "user", mapToResponse(user));
    }

    @PostMapping("/register/player")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> registerPlayer(@Valid @RequestBody RegisterRequest request) {
        User user = authService.register(request, Role.PLAYER);
        return Map.of("message", "Player registered successfully", "user", mapToResponse(user));
    }

    @PostMapping("/login")
    public Map<String, Object> login(@Valid @RequestBody LoginRequest request) {
        User user = authService.login(request);
        return Map.of("message", "Login successful", "user", mapToResponse(user));
    }

    @PostMapping("/logout/{userId}")
    public Map<String, String> logout(@PathVariable Long userId) {
        return Map.of("message", authService.logout(userId));
    }

    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .profilePicture(user.getProfilePicture())
                .phoneNumber(user.getPhoneNumber())
                .dateOfBirth(user.getDateOfBirth())
                .bio(user.getBio())
                .build();
    }
}
