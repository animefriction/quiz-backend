package com.quiztournament.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quiztournament.backend.dto.LoginRequest;
import com.quiztournament.backend.dto.RegisterRequest;
import com.quiztournament.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        // AdminUserInitializer seeds the admin user automatically
    }

    @Test
    void registerPlayer_ShouldReturn201() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newplayer");
        request.setFirstName("New");
        request.setLastName("Player");
        request.setEmail("newplayer@test.com");
        request.setPassword("test123");

        mockMvc.perform(post("/api/auth/register/player")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.user.username", is("newplayer")))
                .andExpect(jsonPath("$.user.role", is("PLAYER")))
                .andExpect(jsonPath("$.message", is("Player registered successfully")));
    }

    @Test
    void registerPlayer_ShouldReturn400OnBlankUsername() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("");
        request.setFirstName("Test");
        request.setLastName("User");
        request.setEmail("blank@test.com");
        request.setPassword("test123");

        mockMvc.perform(post("/api/auth/register/player")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.username").exists());
    }

    @Test
    void registerPlayer_ShouldReturn400OnInvalidEmail() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("emailtest");
        request.setFirstName("Test");
        request.setLastName("User");
        request.setEmail("not-an-email");
        request.setPassword("test123");

        mockMvc.perform(post("/api/auth/register/player")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").exists());
    }

    @Test
    void login_ShouldReturnUserOnValidCredentials() throws Exception {
        // Admin user is seeded by AdminUserInitializer
        LoginRequest request = new LoginRequest();
        request.setUsernameOrEmail("admin");
        request.setPassword("op@1234");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.username", is("admin")))
                .andExpect(jsonPath("$.user.role", is("ADMIN")))
                .andExpect(jsonPath("$.message", is("Login successful")));
    }

    @Test
    void login_ShouldReturn400OnWrongPassword() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsernameOrEmail("admin");
        request.setPassword("wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid username/email or password"));
    }

    @Test
    void login_ShouldReturn404OnNonExistentUser() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsernameOrEmail("nonexistent");
        request.setPassword("password");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void logout_ShouldReturnMessage() throws Exception {
        // Admin user has ID 1
        mockMvc.perform(post("/api/auth/logout/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("User logged out successfully")));
    }
}
