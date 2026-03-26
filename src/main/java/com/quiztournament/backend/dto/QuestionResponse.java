package com.quiztournament.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionResponse {

    private Long id;
    private String type;
    private String questionText;
    private List<String> options;
    private Integer points;

    // Only populated AFTER quiz submission (for feedback)
    private String correctAnswer;
}
