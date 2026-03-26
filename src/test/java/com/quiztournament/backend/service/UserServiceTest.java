package com.quiztournament.backend.service;

import com.quiztournament.backend.dto.UserUpdateRequest;
import com.quiztournament.backend.entity.Role;
import com.quiztournament.backend.entity.User;
import com.quiztournament.backend.exception.ResourceNotFoundException;
import com.quiztournament.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User existingUser;

    @BeforeEach
    void setUp() {
        existingUser = User.builder()
                .id(1L)
                .username("player1")
                .firstName("John")
                .lastName("Doe")
                .email("john@test.com")
                .role(Role.PLAYER)
                .build();
    }

    @Test
    void getUserById_ShouldReturnUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));

        User result = userService.getUserById(1L);

        assertEquals("player1", result.getUsername());
    }

    @Test
    void getUserById_ShouldThrowWhenNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(99L));
    }

    @Test
    void updateProfile_ShouldUpdateAllFields() {
        UserUpdateRequest request = new UserUpdateRequest();
        request.setUsername("updateduser");
        request.setFirstName("Updated");
        request.setLastName("Name");
        request.setEmail("updated@test.com");
        request.setPhoneNumber("123-456-7890");
        request.setBio("New bio");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.findByUsername("updateduser")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("updated@test.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        User result = userService.updateProfile(1L, request);

        assertEquals("updateduser", existingUser.getUsername());
        assertEquals("Updated", existingUser.getFirstName());
        assertEquals("updated@test.com", existingUser.getEmail());
        assertEquals("123-456-7890", existingUser.getPhoneNumber());
        verify(userRepository).save(existingUser);
    }

    @Test
    void updateProfile_ShouldThrowWhenUsernameTaken() {
        User otherUser = User.builder().id(2L).username("taken").build();

        UserUpdateRequest request = new UserUpdateRequest();
        request.setUsername("taken");
        request.setFirstName("Test");
        request.setLastName("User");
        request.setEmail("john@test.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.findByUsername("taken")).thenReturn(Optional.of(otherUser));

        assertThrows(IllegalArgumentException.class,
                () -> userService.updateProfile(1L, request));
    }

    @Test
    void updateProfile_ShouldAllowSameUsernameForSameUser() {
        UserUpdateRequest request = new UserUpdateRequest();
        request.setUsername("player1"); // same as existing
        request.setFirstName("Updated");
        request.setLastName("Name");
        request.setEmail("john@test.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.findByUsername("player1")).thenReturn(Optional.of(existingUser));
        when(userRepository.findByEmail("john@test.com")).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        User result = userService.updateProfile(1L, request);

        assertEquals("Updated", existingUser.getFirstName());
        verify(userRepository).save(existingUser);
    }
}
