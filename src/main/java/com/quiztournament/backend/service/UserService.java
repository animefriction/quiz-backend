package com.quiztournament.backend.service;

import com.quiztournament.backend.dto.UserUpdateRequest;
import com.quiztournament.backend.entity.User;
import com.quiztournament.backend.exception.ResourceNotFoundException;
import com.quiztournament.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }

    public User updateProfile(Long userId, UserUpdateRequest request) {
        User user = getUserById(userId);

        // Check username uniqueness (excluding current user)
        userRepository.findByUsername(request.getUsername())
                .filter(existing -> !existing.getId().equals(userId))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Username is already in use");
                });

        // Check email uniqueness (excluding current user)
        userRepository.findByEmail(request.getEmail())
                .filter(existing -> !existing.getId().equals(userId))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Email is already in use");
                });

        user.setUsername(request.getUsername());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setDateOfBirth(request.getDateOfBirth());
        user.setBio(request.getBio());

        return userRepository.save(user);
    }
}
