package com.quiztournament.backend.controller;

import com.quiztournament.backend.dto.QuizSubmissionRequest;
import com.quiztournament.backend.dto.QuizTournamentResponse;
import com.quiztournament.backend.dto.ScoreResponse;
import com.quiztournament.backend.service.PlayerQuizTournamentService;
import com.quiztournament.backend.service.QuizLikeService;
import com.quiztournament.backend.service.QuizParticipationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/player/tournaments")
@RequiredArgsConstructor
public class PlayerQuizController {

    private final PlayerQuizTournamentService playerQuizTournamentService;
    private final QuizParticipationService quizParticipationService;
    private final QuizLikeService quizLikeService;

    // ---- Tournament browsing by status ----

    @GetMapping("/ongoing")
    public List<QuizTournamentResponse> getOngoingTournaments() {
        return playerQuizTournamentService.getOngoingTournaments();
    }

    @GetMapping("/upcoming")
    public List<QuizTournamentResponse> getUpcomingTournaments() {
        return playerQuizTournamentService.getUpcomingTournaments();
    }

    @GetMapping("/past")
    public List<QuizTournamentResponse> getPastTournaments() {
        return playerQuizTournamentService.getPastTournaments();
    }

    @GetMapping("/participated")
    public List<QuizTournamentResponse> getParticipatedTournaments(@RequestParam Long userId) {
        return playerQuizTournamentService.getParticipatedTournaments(userId);
    }

    // ---- Tournament detail ----

    @GetMapping("/{tournamentId}")
    public QuizTournamentResponse getTournamentById(@PathVariable Long tournamentId) {
        return playerQuizTournamentService.getTournamentById(tournamentId);
    }

    // ---- Quiz participation ----

    @PostMapping("/{tournamentId}/submit")
    @ResponseStatus(HttpStatus.CREATED)
    public ScoreResponse submitQuiz(@PathVariable Long tournamentId,
                                    @Valid @RequestBody QuizSubmissionRequest request) {
        return quizParticipationService.submitQuiz(tournamentId, request);
    }

    // ---- Leaderboard ----

    @GetMapping("/{tournamentId}/scores")
    public List<ScoreResponse> getTournamentScores(@PathVariable Long tournamentId) {
        return quizParticipationService.getTournamentScores(tournamentId);
    }

    // ---- Likes ----

    @PostMapping("/{tournamentId}/like/{userId}")
    public Map<String, Object> likeTournament(@PathVariable Long tournamentId,
                                              @PathVariable Long userId) {
        int totalLikes = quizLikeService.likeTournament(userId, tournamentId);
        return Map.of("message", "Tournament liked successfully", "totalLikes", totalLikes);
    }

    @PostMapping("/{tournamentId}/unlike/{userId}")
    public Map<String, Object> unlikeTournament(@PathVariable Long tournamentId,
                                                @PathVariable Long userId) {
        int totalLikes = quizLikeService.unlikeTournament(userId, tournamentId);
        return Map.of("message", "Tournament unliked successfully", "totalLikes", totalLikes);
    }

    // ---- Additional player feature #1: Search/filter tournaments ----

    @GetMapping("/search")
    public List<QuizTournamentResponse> searchTournaments(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String difficulty) {
        return playerQuizTournamentService.searchTournaments(category, difficulty);
    }

    // ---- Additional player feature #2: Player quiz history ----

    @GetMapping("/history/{userId}")
    public List<ScoreResponse> getPlayerHistory(@PathVariable Long userId) {
        return playerQuizTournamentService.getPlayerHistory(userId);
    }
}
