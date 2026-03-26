package com.quiztournament.backend.config;

import com.quiztournament.backend.entity.Role;
import com.quiztournament.backend.entity.User;
import com.quiztournament.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds the default admin user required by the assignment:
 *   username: admin
 *   password: op@1234
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AdminUserInitializer implements CommandLineRunner {

    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_EMAIL = "admin@quiztournament.local";
    private static final String ADMIN_PASSWORD = "op@1234";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.findByUsername(ADMIN_USERNAME).isPresent()) {
            log.info("Default admin user already exists — skipping seed.");
            return;
        }

        User admin = User.builder()
                .username(ADMIN_USERNAME)
                .firstName("Admin")
                .lastName("User")
                .email(ADMIN_EMAIL)
                .password(passwordEncoder.encode(ADMIN_PASSWORD))
                .role(Role.ADMIN)
                .build();

        userRepository.save(admin);
        log.info("Default admin user created (username: admin, password: op@1234)");
    }
}
