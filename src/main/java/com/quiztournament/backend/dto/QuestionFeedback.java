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
public class QuestionFeedback {

    private Long questionId;
    private String questionText;
    private String playerAnswer;
    private String correctAnswer;
    private Boolean isCorrect;
}
