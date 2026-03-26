package com.quiztournament.backend.service;

import com.quiztournament.backend.dto.QuestionFeedback;
import com.quiztournament.backend.dto.QuizSubmissionRequest;
import com.quiztournament.backend.dto.ScoreResponse;
import com.quiztournament.backend.entity.Question;
import com.quiztournament.backend.entity.QuizAttempt;
import com.quiztournament.backend.entity.QuizTournament;
import com.quiztournament.backend.entity.User;
import com.quiztournament.backend.exception.ResourceNotFoundException;
import com.quiztournament.backend.repository.QuizAttemptRepository;
import com.quiztournament.backend.repository.QuizLikeRepository;
import com.quiztournament.backend.repository.QuizTournamentRepository;
import com.quiztournament.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class QuizParticipationService {

    private final QuizAttemptRepository quizAttemptRepository;
    private final QuizTournamentRepository quizTournamentRepository;
    private final QuizLikeRepository quizLikeRepository;
    private final UserRepository userRepository;

    /**
     * Submits a quiz attempt. Answers are validated server-side
     * against the stored correct answers.
     */
    @Transactional
    public ScoreResponse submitQuiz(Long tournamentId, QuizSubmissionRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + request.getUserId()));

        QuizTournament tournament = quizTournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Quiz tournament not found with id: " + tournamentId));

        // Only ongoing tournaments accept submissions
        if (!"ONGOING".equals(tournament.getStatus())) {
            throw new IllegalArgumentException("Can only submit quizzes for ongoing tournaments");
        }

        // Prevent duplicate submissions
        if (quizAttemptRepository.existsByUserIdAndQuizTournamentId(request.getUserId(), tournamentId)) {
            throw new IllegalArgumentException("You have already completed this tournament");
        }

        // Calculate score server-side and build per-question feedback
        Map<Long, String> answers = request.getAnswers();
        int score = 0;
        int totalAnswered = 0;
        List<QuestionFeedback> feedback = new ArrayList<>();

        for (Question question : tournament.getQuestions()) {
            String playerAnswer = answers.get(question.getId());
            boolean isCorrect = false;

            if (playerAnswer != null) {
                totalAnswered++;
                isCorrect = question.getCorrectAnswer().equalsIgnoreCase(playerAnswer.trim());
                if (isCorrect) {
                    score += question.getPoints();
                }
            }

            feedback.add(QuestionFeedback.builder()
                    .questionId(question.getId())
                    .questionText(question.getQuestionText())
                    .playerAnswer(playerAnswer)
                    .correctAnswer(question.getCorrectAnswer())
                    .isCorrect(isCorrect)
                    .build());
        }

        QuizAttempt attempt = QuizAttempt.builder()
                .user(user)
                .quizTournament(tournament)
                .score(score)
                .totalAnswered(totalAnswered)
                .completed(true)
                .completedAt(LocalDateTime.now())
                .build();

        quizAttemptRepository.save(attempt);

        boolean passed = ((double) score / tournament.getTotalQuestions()) * 100
                >= tournament.getMinPassingScore();

        return ScoreResponse.builder()
                .userId(user.getId())
                .playerName(user.getFirstName() + " " + user.getLastName())
                .quizTournamentId(tournamentId)
                .score(score)
                .totalQuestions(tournament.getTotalQuestions())
                .passed(passed)
                .completedDate(attempt.getCompletedAt())
                .feedback(feedback)
                .build();
    }

    /**
     * Returns the leaderboard for a tournament with all assignment-required
     * aggregate fields: totalPlayers, averageScore, numberOfLikes.
     * Sorted by score descending.
     */
    public List<ScoreResponse> getTournamentScores(Long tournamentId) {
        QuizTournament tournament = quizTournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Quiz tournament not found with id: " + tournamentId));

        List<QuizAttempt> attempts = quizAttemptRepository
                .findByQuizTournamentIdOrderByScoreDesc(tournamentId);

        int totalPlayers = attempts.size();
        double averageScore = attempts.stream()
                .mapToInt(QuizAttempt::getScore)
                .average()
                .orElse(0.0);
        int numberOfLikes = (int) quizLikeRepository.countByQuizTournamentId(tournamentId);

        return attempts.stream()
                .map(attempt -> {
                    boolean passed = ((double) attempt.getScore() / tournament.getTotalQuestions()) * 100
                            >= tournament.getMinPassingScore();

                    return ScoreResponse.builder()
                            .userId(attempt.getUser().getId())
                            .playerName(attempt.getUser().getFirstName() + " " + attempt.getUser().getLastName())
                            .quizTournamentId(tournamentId)
                            .score(attempt.getScore())
                            .totalQuestions(tournament.getTotalQuestions())
                            .passed(passed)
                            .completedDate(attempt.getCompletedAt())
                            .totalPlayers(totalPlayers)
                            .averageScore(Math.round(averageScore * 100.0) / 100.0)
                            .numberOfLikes(numberOfLikes)
                            .build();
                })
                .toList();
    }
}
