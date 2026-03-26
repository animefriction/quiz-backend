package com.quiztournament.backend.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.HtmlUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Integrates with the Open Trivia Database (https://opentdb.com/) to
 * dynamically fetch quiz questions. Supports both multiple-choice and
 * true/false question types.
 */
@Service
public class OpenTdbService {

    private static final String OPEN_TDB_URL =
            "https://opentdb.com/api.php?amount={amount}&category={categoryId}&difficulty={difficulty}";

    private final RestTemplate restTemplate;

    public OpenTdbService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    /**
     * Fetches questions from OpenTDB. Returns a mix of multiple-choice
     * and true/false questions depending on availability.
     */
    public List<OpenTdbQuestionData> fetchQuestions(Integer categoryId, String difficulty, int amount) {
        ResponseEntity<OpenTdbResponse> responseEntity = restTemplate.getForEntity(
                OPEN_TDB_URL,
                OpenTdbResponse.class,
                amount,
                categoryId,
                difficulty.toLowerCase()
        );

        OpenTdbResponse response = responseEntity.getBody();

        if (response == null) {
            throw new IllegalArgumentException("No response was returned from OpenTDB");
        }

        if (response.responseCode() != 0) {
            throw new IllegalArgumentException(buildErrorMessage(response.responseCode()));
        }

        if (response.results() == null || response.results().isEmpty()) {
            throw new IllegalArgumentException("No questions were returned from OpenTDB");
        }

        return response.results()
                .stream()
                .map(this::mapQuestion)
                .toList();
    }

    private OpenTdbQuestionData mapQuestion(OpenTdbQuestionResult result) {
        String correctAnswer = HtmlUtils.htmlUnescape(result.correctAnswer());
        String type = result.type(); // "multiple" or "boolean"

        List<String> incorrectAnswers = result.incorrectAnswers()
                .stream()
                .map(HtmlUtils::htmlUnescape)
                .toList();

        // Build shuffled options list
        List<String> options = new ArrayList<>(incorrectAnswers);
        options.add(correctAnswer);
        Collections.shuffle(options);

        return new OpenTdbQuestionData(
                type,
                HtmlUtils.htmlUnescape(result.category()),
                HtmlUtils.htmlUnescape(result.question()),
                correctAnswer,
                incorrectAnswers,
                options
        );
    }

    private String buildErrorMessage(int responseCode) {
        return switch (responseCode) {
            case 1 -> "OpenTDB does not have enough questions for this category and difficulty";
            case 2 -> "Invalid OpenTDB request parameters";
            case 5 -> "OpenTDB rate limit reached. Try again in a few seconds";
            default -> "OpenTDB request failed with response code: " + responseCode;
        };
    }

    // ---- Data records ----

    public record OpenTdbQuestionData(
            String type,
            String category,
            String questionText,
            String correctAnswer,
            List<String> incorrectAnswers,
            List<String> options
    ) {
    }

    public record OpenTdbResponse(
            @JsonProperty("response_code") Integer responseCode,
            List<OpenTdbQuestionResult> results
    ) {
    }

    public record OpenTdbQuestionResult(
            String category,
            String difficulty,
            String type,
            String question,
            @JsonProperty("correct_answer") String correctAnswer,
            @JsonProperty("incorrect_answers") List<String> incorrectAnswers
    ) {
    }
}
