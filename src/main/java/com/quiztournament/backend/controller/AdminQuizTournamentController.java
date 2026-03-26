package com.quiztournament.backend.controller;

import com.quiztournament.backend.dto.QuizTournamentCreateRequest;
import com.quiztournament.backend.dto.QuizTournamentResponse;
import com.quiztournament.backend.dto.QuizTournamentUpdateRequest;
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
@RequestMapping("/api/admin/tournaments")
@RequiredArgsConstructor
public class AdminQuizTournamentController {

    private final AdminQuizTournamentService adminQuizTournamentService;

    @GetMapping
    public List<QuizTournamentResponse> getAllTournaments() {
        return adminQuizTournamentService.getAllTournaments();
    }

    @GetMapping("/{tournamentId}")
    public QuizTournamentResponse getTournamentById(@PathVariable Long tournamentId) {
        return adminQuizTournamentService.getTournamentById(tournamentId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public QuizTournamentResponse createTournament(
            @Valid @RequestBody QuizTournamentCreateRequest request) {
        return adminQuizTournamentService.createTournament(request);
    }

    @PutMapping("/{tournamentId}")
    public QuizTournamentResponse updateTournament(
            @PathVariable Long tournamentId,
            @Valid @RequestBody QuizTournamentUpdateRequest request) {
        return adminQuizTournamentService.updateTournament(tournamentId, request);
    }

    @DeleteMapping("/{tournamentId}")
    public Map<String, String> deleteTournament(@PathVariable Long tournamentId) {
        adminQuizTournamentService.deleteTournament(tournamentId);
        return Map.of("message", "Quiz tournament deleted successfully");
    }
}
