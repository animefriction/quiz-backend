package com.quiztournament.backend.service;

import com.quiztournament.backend.dto.QuestionResponse;
import com.quiztournament.backend.dto.QuizTournamentResponse;
import com.quiztournament.backend.dto.ScoreResponse;
import com.quiztournament.backend.entity.Question;
import com.quiztournament.backend.entity.QuizTournament;
import com.quiztournament.backend.exception.ResourceNotFoundException;
import com.quiztournament.backend.repository.QuizAttemptRepository;
import com.quiztournament.backend.repository.QuizLikeRepository;
import com.quiztournament.backend.repository.QuizTournamentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlayerQuizTournamentService {

    private final QuizTournamentRepository quizTournamentRepository;
    private final QuizLikeRepository quizLikeRepository;
    private final QuizAttemptRepository quizAttemptRepository;

    public List<QuizTournamentResponse> getOngoingTournaments() {
        return filterByStatus("ONGOING");
    }

    public List<QuizTournamentResponse> getUpcomingTournaments() {
        return filterByStatus("UPCOMING");
    }

    public List<QuizTournamentResponse> getPastTournaments() {
        return filterByStatus("PAST");
    }

    /**
     * Returns tournaments that the given player has participated in.
     */
    public List<QuizTournamentResponse> getParticipatedTournaments(Long userId) {
        Set<Long> participatedIds = quizAttemptRepository.findByUserId(userId)
                .stream()
                .map(attempt -> attempt.getQuizTournament().getId())
                .collect(Collectors.toSet());

        return quizTournamentRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .filter(t -> participatedIds.contains(t.getId()))
                .map(this::mapToResponse)
                .toList();
    }

    public QuizTournamentResponse getTournamentById(Long tournamentId) {
        QuizTournament tournament = quizTournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Quiz tournament not found with id: " + tournamentId));
        return mapToResponse(tournament);
    }

    /**
     * Additional player feature #1: Search/filter tournaments by category
     * and/or difficulty.
     */
    public List<QuizTournamentResponse> searchTournaments(String category, String difficulty) {
        return quizTournamentRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .filter(t -> {
                    boolean matchesCategory = category == null || category.isBlank()
                            || t.getCategory().equalsIgnoreCase(category);
                    boolean matchesDifficulty = difficulty == null || difficulty.isBlank()
                            || t.getDifficulty().equalsIgnoreCase(difficulty);
                    return matchesCategory && matchesDifficulty;
                })
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Additional player feature #2: Player's personal quiz history
     * showing all attempts with scores and dates.
     */
    public List<ScoreResponse> getPlayerHistory(Long userId) {
        return quizAttemptRepository.findByUserId(userId)
                .stream()
                .map(attempt -> {
                    QuizTournament t = attempt.getQuizTournament();
                    boolean passed = ((double) attempt.getScore() / t.getTotalQuestions()) * 100
                            >= t.getMinPassingScore();
                    return ScoreResponse.builder()
                            .userId(attempt.getUser().getId())
                            .playerName(attempt.getUser().getFirstName() + " " + attempt.getUser().getLastName())
                            .quizTournamentId(t.getId())
                            .score(attempt.getScore())
                            .totalQuestions(t.getTotalQuestions())
                            .passed(passed)
                            .completedDate(attempt.getCompletedAt())
                            .build();
                })
                .toList();
    }

    // ---- Helpers ----

    private List<QuizTournamentResponse> filterByStatus(String status) {
        return quizTournamentRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .filter(t -> t.getStatus().equals(status))
                .map(this::mapToResponse)
                .toList();
    }

    private QuizTournamentResponse mapToResponse(QuizTournament tournament) {
        return QuizTournamentResponse.builder()
                .id(tournament.getId())
                .name(tournament.getName())
                .category(tournament.getCategory())
                .categoryId(tournament.getCategoryId())
                .difficulty(tournament.getDifficulty())
                .status(tournament.getStatus())
                .startDate(tournament.getStartDate())
                .endDate(tournament.getEndDate())
                .minPassingScore(tournament.getMinPassingScore())
                .totalQuestions(tournament.getTotalQuestions())
                .createdAt(tournament.getCreatedAt())
                .createdByUserId(tournament.getCreatedBy().getId())
                .createdByUsername(tournament.getCreatedBy().getUsername())
                .totalLikes((int) quizLikeRepository.countByQuizTournamentId(tournament.getId()))
                .questions(tournament.getQuestions().stream()
                        .map(this::mapQuestionToResponse)
                        .toList())
                .build();
    }

    private QuestionResponse mapQuestionToResponse(Question question) {
        return QuestionResponse.builder()
                .id(question.getId())
                .type(question.getType())
                .questionText(question.getQuestionText())
                .options(question.getAllOptions())
                .points(question.getPoints())
                .build();
    }
}
