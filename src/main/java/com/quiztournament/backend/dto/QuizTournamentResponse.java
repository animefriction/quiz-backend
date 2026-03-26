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
public class QuizTournamentResponse {

    private Long id;
    private String name;
    private String category;
    private Integer categoryId;
    private String difficulty;
    private String status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer minPassingScore;
    private Integer totalQuestions;
    private LocalDateTime createdAt;
    private Long createdByUserId;
    private String createdByUsername;
    private Integer totalLikes;
    private List<QuestionResponse> questions;
}
