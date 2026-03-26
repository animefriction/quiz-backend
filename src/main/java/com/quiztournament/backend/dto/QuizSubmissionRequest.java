package com.quiztournament.backend.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class QuizSubmissionRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotEmpty(message = "Answers cannot be empty")
    private Map<Long, String> answers; // questionId -> selected answer
}
