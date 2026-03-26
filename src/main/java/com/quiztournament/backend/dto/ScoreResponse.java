package com.quiztournament.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScoreResponse {

    private Long userId;
    private String playerName;
    private Long quizTournamentId;
    private Integer score;
    private Integer totalQuestions;
    private Boolean passed;
    private LocalDateTime completedDate;

    // Tournament-level aggregates (populated in leaderboard view)
    private Integer totalPlayers;
    private Double averageScore;
    private Integer numberOfLikes;

    // Per-question feedback (populated only on submission response)
    private List<QuestionFeedback> feedback;
}
