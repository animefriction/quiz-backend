package com.quiztournament.backend.service;

import com.quiztournament.backend.dto.LoginRequest;
import com.quiztournament.backend.dto.RegisterRequest;
import com.quiztournament.backend.entity.Role;
import com.quiztournament.backend.entity.User;
import com.quiztournament.backend.exception.ResourceNotFoundException;
import com.quiztournament.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Registers a new user with the given role.
     */
    public User register(RegisterRequest request, Role role) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username is already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already in use");
        }

        User user = User.builder()
                .username(request.getUsername())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .profilePicture(request.getProfilePicture())
                .build();

        return userRepository.save(user);
    }

    /**
     * Authenticates a user by username/email and password.
     */
    public User login(LoginRequest request) {
        User user = userRepository.findByUsernameOrEmail(
                        request.getUsernameOrEmail(), request.getUsernameOrEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid username/email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid username/email or password");
        }

        return user;
    }

    /**
     * Placeholder logout — in a stateless REST API the client simply
     * discards its session/token. Kept for assignment completeness.
     */
    public String logout(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
        return "User logged out successfully";
    }
}
