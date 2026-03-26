package com.quiztournament.backend.repository;

import com.quiztournament.backend.entity.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {

    Optional<QuizAttempt> findByUserIdAndQuizTournamentId(Long userId, Long quizTournamentId);

    List<QuizAttempt> findByQuizTournamentIdOrderByScoreDesc(Long quizTournamentId);

    List<QuizAttempt> findByUserId(Long userId);

    boolean existsByUserIdAndQuizTournamentId(Long userId, Long quizTournamentId);
}
