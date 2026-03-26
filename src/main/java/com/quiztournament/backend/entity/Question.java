package com.quiztournament.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "questions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String type; // "multiple" or "boolean"

    @Column(nullable = false, length = 1000)
    private String questionText;

    @Column(nullable = false)
    private String correctAnswer;

    @Column(nullable = false)
    private String option1;

    @Column(nullable = false)
    private String option2;

    @Column
    private String option3; // null for true/false questions

    @Column
    private String option4; // null for true/false questions

    @Column(nullable = false)
    private Integer points;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_tournament_id", nullable = false)
    private QuizTournament quizTournament;

    /**
     * Returns all non-null options as a list.
     */
    public List<String> getAllOptions() {
        List<String> options = new ArrayList<>();
        options.add(option1);
        options.add(option2);
        if (option3 != null) options.add(option3);
        if (option4 != null) options.add(option4);
        return options;
    }
}
