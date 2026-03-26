package com.quiztournament.backend.repository;

import com.quiztournament.backend.entity.QuizLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QuizLikeRepository extends JpaRepository<QuizLike, Long> {

    boolean existsByUserIdAndQuizTournamentId(Long userId, Long quizTournamentId);

    Optional<QuizLike> findByUserIdAndQuizTournamentId(Long userId, Long quizTournamentId);

    long countByQuizTournamentId(Long quizTournamentId);
}
