package com.quiztournament.backend.service;

import com.quiztournament.backend.dto.QuestionResponse;
import com.quiztournament.backend.dto.QuizTournamentCreateRequest;
import com.quiztournament.backend.dto.QuizTournamentResponse;
import com.quiztournament.backend.dto.QuizTournamentUpdateRequest;
import com.quiztournament.backend.entity.Question;
import com.quiztournament.backend.entity.QuizTournament;
import com.quiztournament.backend.entity.User;
import com.quiztournament.backend.exception.ResourceNotFoundException;
import com.quiztournament.backend.repository.QuizLikeRepository;
import com.quiztournament.backend.repository.QuizTournamentRepository;
import com.quiztournament.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminQuizTournamentService {

    private static final int TOURNAMENT_QUESTION_COUNT = 10;

    private final OpenTdbService openTdbService;
    private final QuizTournamentRepository quizTournamentRepository;
    private final QuizLikeRepository quizLikeRepository;
    private final UserRepository userRepository;

    public List<QuizTournamentResponse> getAllTournaments() {
        return quizTournamentRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public QuizTournamentResponse getTournamentById(Long tournamentId) {
        return mapToResponse(findTournament(tournamentId));
    }

    @Transactional
    public QuizTournamentResponse createTournament(QuizTournamentCreateRequest request) {
        User createdBy = userRepository.findById(request.getCreatedByUserId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + request.getCreatedByUserId()));

        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new IllegalArgumentException("End date must be after start date");
        }

        // Fetch 10 questions dynamically from OpenTDB
        List<OpenTdbService.OpenTdbQuestionData> fetchedQuestions =
                openTdbService.fetchQuestions(
                        request.getCategoryId(),
                        request.getDifficulty(),
                        TOURNAMENT_QUESTION_COUNT
                );

        QuizTournament tournament = QuizTournament.builder()
                .name(request.getName())
                .category(fetchedQuestions.get(0).category())
                .categoryId(request.getCategoryId())
                .difficulty(request.getDifficulty())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .minPassingScore(request.getMinPassingScore())
                .totalQuestions(fetchedQuestions.size())
                .createdAt(LocalDateTime.now())
                .createdBy(createdBy)
                .build();

        // Map fetched questions to Question entities
        List<Question> questions = fetchedQuestions.stream()
                .map(q -> {
                    List<String> options = q.options();
                    return Question.builder()
                            .type(q.type())
                            .questionText(q.questionText())
                            .correctAnswer(q.correctAnswer())
                            .option1(options.get(0))
                            .option2(options.get(1))
                            .option3(options.size() > 2 ? options.get(2) : null)
                            .option4(options.size() > 3 ? options.get(3) : null)
                            .points(1)
                            .quizTournament(tournament)
                            .build();
                })
                .toList();

        tournament.setQuestions(questions);

        return mapToResponse(quizTournamentRepository.save(tournament));
    }

    public QuizTournamentResponse updateTournament(Long tournamentId, QuizTournamentUpdateRequest request) {
        QuizTournament tournament = findTournament(tournamentId);

        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new IllegalArgumentException("End date must be after start date");
        }

        tournament.setName(request.getName());
        tournament.setStartDate(request.getStartDate());
        tournament.setEndDate(request.getEndDate());

        return mapToResponse(quizTournamentRepository.save(tournament));
    }

    public void deleteTournament(Long tournamentId) {
        QuizTournament tournament = findTournament(tournamentId);
        quizTournamentRepository.delete(tournament);
    }

    // ---- Helpers ----

    private QuizTournament findTournament(Long tournamentId) {
        return quizTournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Quiz tournament not found with id: " + tournamentId));
    }

    private QuizTournamentResponse mapToResponse(QuizTournament tournament) {
        return QuizTournamentResponse.builder()
                .id(tournament.getId())
                .name(tournament.getName())
                .category(tournament.getCategory())
                .categoryId(tournament.getCategoryId())
                .difficulty(tournament.getDifficulty())
                .status(tournament.getStatus())
                .startDate(tournament.getStartDate())
                .endDate(tournament.getEndDate())
                .minPassingScore(tournament.getMinPassingScore())
                .totalQuestions(tournament.getTotalQuestions())
                .createdAt(tournament.getCreatedAt())
                .createdByUserId(tournament.getCreatedBy().getId())
                .createdByUsername(tournament.getCreatedBy().getUsername())
                .totalLikes((int) quizLikeRepository.countByQuizTournamentId(tournament.getId()))
                .questions(tournament.getQuestions().stream()
                        .map(this::mapQuestionToResponse)
                        .toList())
                .build();
    }

    private QuestionResponse mapQuestionToResponse(Question question) {
        return QuestionResponse.builder()
                .id(question.getId())
                .type(question.getType())
                .questionText(question.getQuestionText())
                .options(question.getAllOptions())
                .points(question.getPoints())
                .build();
    }
}
