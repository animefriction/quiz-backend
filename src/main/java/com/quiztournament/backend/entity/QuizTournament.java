package com.quiztournament.backend.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quiz_tournaments")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizTournament {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private Integer categoryId;

    @Column(nullable = false)
    private String difficulty;

    @Column(nullable = false)
    private LocalDateTime startDate;

    @Column(nullable = false)
    private LocalDateTime endDate;

    @Column(nullable = false)
    private Integer minPassingScore;

    @Column(nullable = false)
    private Integer totalQuestions;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private User createdBy;

    @Builder.Default
    @OneToMany(mappedBy = "quizTournament", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Question> questions = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "quizTournament", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuizAttempt> attempts = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "quizTournament", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuizLike> likes = new ArrayList<>();

    /**
     * Derives tournament status from start and end dates.
     * No hardcoded status column needed.
     */
    public String getStatus() {
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(startDate)) {
            return "UPCOMING";
        } else if (now.isAfter(endDate)) {
            return "PAST";
        } else {
            return "ONGOING";
        }
    }
}
