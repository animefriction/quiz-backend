package com.quiztournament.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Provides a BCryptPasswordEncoder bean for hashing passwords.
 * We are NOT using Spring Security's full filter chain — just the
 * encoder utility. This avoids adding spring-boot-starter-security
 * which would lock all endpoints behind authentication by default.
 */
@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
