package com.quiztournament.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TournamentAnalyticsResponse {

    private Long tournamentId;
    private String tournamentName;
    private Integer totalAttempts;
    private Integer totalQuestions;
    private Double averageScore;
    private Integer highestScore;
    private Integer lowestScore;
    private Double passRate;
    private Integer totalLikes;
}
