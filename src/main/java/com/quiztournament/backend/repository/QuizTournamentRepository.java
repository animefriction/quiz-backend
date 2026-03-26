package com.quiztournament.backend.repository;

import com.quiztournament.backend.entity.QuizTournament;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizTournamentRepository extends JpaRepository<QuizTournament, Long> {

    List<QuizTournament> findAllByOrderByCreatedAtDesc();
}
