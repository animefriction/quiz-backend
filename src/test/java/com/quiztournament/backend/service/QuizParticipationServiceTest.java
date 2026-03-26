package com.quiztournament.backend.service;

import com.quiztournament.backend.dto.QuizSubmissionRequest;
import com.quiztournament.backend.dto.ScoreResponse;
import com.quiztournament.backend.entity.Question;
import com.quiztournament.backend.entity.QuizAttempt;
import com.quiztournament.backend.entity.QuizTournament;
import com.quiztournament.backend.entity.Role;
import com.quiztournament.backend.entity.User;
import com.quiztournament.backend.exception.ResourceNotFoundException;
import com.quiztournament.backend.repository.QuizAttemptRepository;
import com.quiztournament.backend.repository.QuizLikeRepository;
import com.quiztournament.backend.repository.QuizTournamentRepository;
import com.quiztournament.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuizParticipationServiceTest {

    @Mock
    private QuizAttemptRepository quizAttemptRepository;

    @Mock
    private QuizTournamentRepository quizTournamentRepository;

    @Mock
    private QuizLikeRepository quizLikeRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private QuizParticipationService quizParticipationService;

    private User player;
    private QuizTournament ongoingTournament;
    private Question question1;
    private Question question2;

    @BeforeEach
    void setUp() {
        player = User.builder()
                .id(1L)
                .username("player1")
                .firstName("John")
                .lastName("Doe")
                .email("player@test.com")
                .role(Role.PLAYER)
                .build();

        ongoingTournament = QuizTournament.builder()
                .id(1L)
                .name("Science Quiz")
                .category("Science")
                .categoryId(17)
                .difficulty("medium")
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(5))
                .minPassingScore(60)
                .totalQuestions(2)
                .createdAt(LocalDateTime.now().minusDays(2))
                .build();

        question1 = Question.builder()
                .id(1L)
                .questionText("What is H2O?")
                .correctAnswer("Water")
                .type("multiple")
                .option1("Water")
                .option2("Oxygen")
                .option3("Hydrogen")
                .option4("Helium")
                .points(1)
                .quizTournament(ongoingTournament)
                .build();

        question2 = Question.builder()
                .id(2L)
                .questionText("Is the sky blue?")
                .correctAnswer("True")
                .type("boolean")
                .option1("True")
                .option2("False")
                .points(1)
                .quizTournament(ongoingTournament)
                .build();

        ongoingTournament.setQuestions(List.of(question1, question2));
    }

    @Test
    void submitQuiz_ShouldCalculateScoreCorrectly() {
        QuizSubmissionRequest request = new QuizSubmissionRequest();
        request.setUserId(1L);
        request.setAnswers(Map.of(1L, "Water", 2L, "True"));

        when(userRepository.findById(1L)).thenReturn(Optional.of(player));
        when(quizTournamentRepository.findById(1L)).thenReturn(Optional.of(ongoingTournament));
        when(quizAttemptRepository.existsByUserIdAndQuizTournamentId(1L, 1L)).thenReturn(false);
        when(quizAttemptRepository.save(any(QuizAttempt.class))).thenAnswer(inv -> inv.getArgument(0));

        ScoreResponse result = quizParticipationService.submitQuiz(1L, request);

        assertNotNull(result);
        assertEquals(2, result.getScore());
        assertEquals(2, result.getTotalQuestions());
        assertTrue(result.getPassed()); // 100% >= 60%
        assertNotNull(result.getFeedback());
        assertEquals(2, result.getFeedback().size());
        assertTrue(result.getFeedback().get(0).getIsCorrect());
        assertTrue(result.getFeedback().get(1).getIsCorrect());
        assertEquals("Water", result.getFeedback().get(0).getCorrectAnswer());
        verify(quizAttemptRepository).save(any(QuizAttempt.class));
    }

    @Test
    void submitQuiz_ShouldScorePartialAnswersCorrectly() {
        QuizSubmissionRequest request = new QuizSubmissionRequest();
        request.setUserId(1L);
        request.setAnswers(Map.of(1L, "Oxygen", 2L, "True")); // 1 wrong, 1 correct

        when(userRepository.findById(1L)).thenReturn(Optional.of(player));
        when(quizTournamentRepository.findById(1L)).thenReturn(Optional.of(ongoingTournament));
        when(quizAttemptRepository.existsByUserIdAndQuizTournamentId(1L, 1L)).thenReturn(false);
        when(quizAttemptRepository.save(any(QuizAttempt.class))).thenAnswer(inv -> inv.getArgument(0));

        ScoreResponse result = quizParticipationService.submitQuiz(1L, request);

        assertEquals(1, result.getScore());
        assertEquals(2, result.getTotalQuestions());
        assertNotNull(result.getFeedback());
        assertFalse(result.getFeedback().get(0).getIsCorrect()); // Oxygen != Water
        assertTrue(result.getFeedback().get(1).getIsCorrect());  // True == True
    }

    @Test
    void submitQuiz_ShouldRejectDuplicateSubmission() {
        QuizSubmissionRequest request = new QuizSubmissionRequest();
        request.setUserId(1L);
        request.setAnswers(Map.of(1L, "Water"));

        when(userRepository.findById(1L)).thenReturn(Optional.of(player));
        when(quizTournamentRepository.findById(1L)).thenReturn(Optional.of(ongoingTournament));
        when(quizAttemptRepository.existsByUserIdAndQuizTournamentId(1L, 1L)).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> quizParticipationService.submitQuiz(1L, request));
        verify(quizAttemptRepository, never()).save(any());
    }

    @Test
    void submitQuiz_ShouldRejectNonOngoingTournament() {
        QuizTournament futureTournament = QuizTournament.builder()
                .id(2L)
                .name("Future Quiz")
                .startDate(LocalDateTime.now().plusDays(5))
                .endDate(LocalDateTime.now().plusDays(10))
                .totalQuestions(10)
                .minPassingScore(50)
                .build();

        QuizSubmissionRequest request = new QuizSubmissionRequest();
        request.setUserId(1L);
        request.setAnswers(Map.of(1L, "Water"));

        when(userRepository.findById(1L)).thenReturn(Optional.of(player));
        when(quizTournamentRepository.findById(2L)).thenReturn(Optional.of(futureTournament));

        assertThrows(IllegalArgumentException.class,
                () -> quizParticipationService.submitQuiz(2L, request));
    }

    @Test
    void submitQuiz_ShouldThrowOnUserNotFound() {
        QuizSubmissionRequest request = new QuizSubmissionRequest();
        request.setUserId(99L);
        request.setAnswers(Map.of(1L, "Water"));

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> quizParticipationService.submitQuiz(1L, request));
    }

    @Test
    void getTournamentScores_ShouldReturnSortedLeaderboard() {
        User player2 = User.builder()
                .id(2L).username("player2").firstName("Jane").lastName("Doe").build();

        QuizAttempt attempt1 = QuizAttempt.builder()
                .user(player).quizTournament(ongoingTournament)
                .score(2).totalAnswered(2).completed(true)
                .completedAt(LocalDateTime.now()).build();

        QuizAttempt attempt2 = QuizAttempt.builder()
                .user(player2).quizTournament(ongoingTournament)
                .score(1).totalAnswered(2).completed(true)
                .completedAt(LocalDateTime.now()).build();

        when(quizTournamentRepository.findById(1L)).thenReturn(Optional.of(ongoingTournament));
        when(quizAttemptRepository.findByQuizTournamentIdOrderByScoreDesc(1L))
                .thenReturn(List.of(attempt1, attempt2));
        when(quizLikeRepository.countByQuizTournamentId(1L)).thenReturn(3L);

        List<ScoreResponse> results = quizParticipationService.getTournamentScores(1L);

        assertEquals(2, results.size());
        assertEquals(2, results.get(0).getTotalPlayers());
        assertEquals(1.5, results.get(0).getAverageScore());
        assertEquals(3, results.get(0).getNumberOfLikes());
        assertEquals(2, results.get(0).getScore()); // highest first
        assertEquals(1, results.get(1).getScore());
    }
}
