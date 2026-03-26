package com.quiztournament.backend.repository;

import com.quiztournament.backend.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    List<Question> findByQuizTournamentId(Long quizTournamentId);
}
