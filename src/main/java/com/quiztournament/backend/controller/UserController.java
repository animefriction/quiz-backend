package com.quiztournament.backend.controller;

import com.quiztournament.backend.dto.UserResponse;
import com.quiztournament.backend.dto.UserUpdateRequest;
import com.quiztournament.backend.entity.User;
import com.quiztournament.backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/{userId}")
    public UserResponse getProfile(@PathVariable Long userId) {
        return mapToResponse(userService.getUserById(userId));
    }

    @PutMapping("/{userId}")
    public Map<String, Object> updateProfile(@PathVariable Long userId,
                                             @Valid @RequestBody UserUpdateRequest request) {
        User user = userService.updateProfile(userId, request);
        return Map.of("message", "Profile updated successfully", "user", mapToResponse(user));
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
