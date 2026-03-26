package com.quiztournament.backend.controller;

import com.quiztournament.backend.dto.QuizTournamentCreateRequest;
import com.quiztournament.backend.dto.QuizTournamentResponse;
import com.quiztournament.backend.dto.QuizTournamentUpdateRequest;
import com.quiztournament.backend.dto.TournamentAnalyticsResponse;
import com.quiztournament.backend.dto.UserResponse;
import com.quiztournament.backend.entity.Role;
import com.quiztournament.backend.entity.User;
import com.quiztournament.backend.repository.UserRepository;
import com.quiztournament.backend.service.AdminQuizTournamentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminQuizTournamentController {

    private final AdminQuizTournamentService adminQuizTournamentService;
    private final UserRepository userRepository;

    // ---- Tournament CRUD ----

    @GetMapping("/tournaments")
    public List<QuizTournamentResponse> getAllTournaments() {
        return adminQuizTournamentService.getAllTournaments();
    }

    @GetMapping("/tournaments/{tournamentId}")
    public QuizTournamentResponse getTournamentById(@PathVariable Long tournamentId) {
        return adminQuizTournamentService.getTournamentById(tournamentId);
    }

    @PostMapping("/tournaments")
    @ResponseStatus(HttpStatus.CREATED)
    public QuizTournamentResponse createTournament(
            @Valid @RequestBody QuizTournamentCreateRequest request) {
        return adminQuizTournamentService.createTournament(request);
    }

    @PutMapping("/tournaments/{tournamentId}")
    public QuizTournamentResponse updateTournament(
            @PathVariable Long tournamentId,
            @Valid @RequestBody QuizTournamentUpdateRequest request) {
        return adminQuizTournamentService.updateTournament(tournamentId, request);
    }

    @DeleteMapping("/tournaments/{tournamentId}")
    public Map<String, String> deleteTournament(@PathVariable Long tournamentId) {
        adminQuizTournamentService.deleteTournament(tournamentId);
        return Map.of("message", "Quiz tournament deleted successfully");
    }

    // ---- Additional admin feature #1: Tournament analytics ----

    @GetMapping("/tournaments/{tournamentId}/analytics")
    public TournamentAnalyticsResponse getTournamentAnalytics(@PathVariable Long tournamentId) {
        return adminQuizTournamentService.getTournamentAnalytics(tournamentId);
    }

    // ---- View tournament likes (assignment requirement) ----

    @GetMapping("/tournaments/{tournamentId}/likes")
    public Map<String, Object> getTournamentLikes(@PathVariable Long tournamentId) {
        int count = adminQuizTournamentService.getTournamentLikeCount(tournamentId);
        return Map.of("tournamentId", tournamentId, "totalLikes", count);
    }

    // ---- Additional admin feature #2: View all players ----

    @GetMapping("/users")
    public List<UserResponse> getAllPlayers() {
        return userRepository.findByRole(Role.PLAYER).stream()
                .map(this::mapToUserResponse)
                .toList();
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }
}
