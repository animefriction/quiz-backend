package com.quiztournament.backend.service;

import com.quiztournament.backend.entity.QuizTournament;
import com.quiztournament.backend.entity.Role;
import com.quiztournament.backend.entity.User;
import com.quiztournament.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Handles all outgoing email for the application:
 * - Password reset instructions
 * - New tournament notifications to all players
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final UserRepository userRepository;

    /**
     * Sends a password reset email containing the reset token.
     */
    public void sendPasswordResetEmail(User user, String token) {
        String subject = "Quiz Tournament — Password Reset Request";
        String body = String.format(
                "Hi %s,\n\n"
                + "You requested a password reset. Use the following token to reset your password:\n\n"
                + "    %s\n\n"
                + "This token will expire in 1 hour.\n\n"
                + "If you did not request this, please ignore this email.\n\n"
                + "— Quiz Tournament Team",
                user.getFirstName(), token
        );

        sendEmail(user.getEmail(), subject, body);
    }

    /**
     * Notifies all player users that a new quiz tournament has been created.
     * Admin users are excluded as per the assignment requirement.
     */
    @Async
    public void sendTournamentCreatedNotification(QuizTournament tournament) {
        List<User> players = userRepository.findByRole(Role.PLAYER);

        if (players.isEmpty()) {
            log.info("No player users to notify about tournament: {}", tournament.getName());
            return;
        }

        String subject = "New Quiz Tournament Available — " + tournament.getName();
        String body = String.format(
                "A new quiz tournament has been created!\n\n"
                + "Name: %s\n"
                + "Category: %s\n"
                + "Difficulty: %s\n"
                + "Starts: %s\n"
                + "Ends: %s\n\n"
                + "Log in to participate and compete for the top spot on the leaderboard!\n\n"
                + "— Quiz Tournament Team",
                tournament.getName(),
                tournament.getCategory(),
                tournament.getDifficulty(),
                tournament.getStartDate(),
                tournament.getEndDate()
        );

        for (User player : players) {
            try {
                sendEmail(player.getEmail(), subject, body);
            } catch (Exception e) {
                log.warn("Failed to send tournament notification to {}: {}",
                        player.getEmail(), e.getMessage());
            }
        }

        log.info("Tournament notification sent to {} players for tournament: {}",
                players.size(), tournament.getName());
    }

    private void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            message.setFrom("noreply@quiztournament.local");
            mailSender.send(message);
            log.info("Email sent to {}: {}", to, subject);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }
}
