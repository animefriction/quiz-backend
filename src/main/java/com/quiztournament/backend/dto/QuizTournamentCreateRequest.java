package com.quiztournament.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class QuizTournamentCreateRequest {

    @NotBlank(message = "Tournament name is required")
    private String name;

    @NotNull(message = "Category ID is required")
    private Integer categoryId;

    @NotBlank(message = "Difficulty is required (easy, medium, hard)")
    private String difficulty;

    @NotNull(message = "Start date is required")
    private LocalDateTime startDate;

    @NotNull(message = "End date is required")
    private LocalDateTime endDate;

    @NotNull(message = "Minimum passing score is required")
    @Min(value = 1, message = "Minimum passing score must be at least 1%")
    @Max(value = 100, message = "Minimum passing score cannot exceed 100%")
    private Integer minPassingScore;

    @NotNull(message = "Created by user ID is required")
    private Long createdByUserId;
}
