package com.quiztournament.backend.service;

import com.quiztournament.backend.dto.LoginRequest;
import com.quiztournament.backend.dto.RegisterRequest;
import com.quiztournament.backend.entity.PasswordResetToken;
import com.quiztournament.backend.entity.Role;
import com.quiztournament.backend.entity.User;
import com.quiztournament.backend.exception.ResourceNotFoundException;
import com.quiztournament.backend.repository.PasswordResetTokenRepository;
import com.quiztournament.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;

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

    /**
     * Generates a password reset token and emails the instructions.
     * If the email doesn't exist, returns a generic message to prevent info leaking.
     */
    public String forgotPassword(String email) {
        return userRepository.findByEmail(email)
                .map(user -> {
                    String token = UUID.randomUUID().toString();

                    PasswordResetToken resetToken = PasswordResetToken.builder()
                            .token(token)
                            .user(user)
                            .expiresAt(LocalDateTime.now().plusHours(1))
                            .used(false)
                            .build();

                    passwordResetTokenRepository.save(resetToken);
                    emailService.sendPasswordResetEmail(user, token);

                    return "If an account with that email exists, a reset link has been sent.";
                })
                .orElse("If an account with that email exists, a reset link has been sent.");
    }

    /**
     * Validates the token and resets the password.
     */
    @Transactional
    public String resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired reset token"));

        if (resetToken.getUsed()) {
            throw new IllegalArgumentException("This reset token has already been used");
        }

        if (resetToken.isExpired()) {
            throw new IllegalArgumentException("This reset token has expired");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        return "Password has been reset successfully";
    }
}
