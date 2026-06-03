package com.quiztournament.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generates short, learner-friendly explanations for INCORRECT quiz answers
 * by calling the Anthropic Claude Messages API.
 *
 * Design notes:
 *  - Grounded generation: the verified correct answer is passed into the prompt,
 *    so the model explains a known-correct fact rather than deciding what is correct.
 *    This is the main defence against hallucination for this feature.
 *  - Graceful failure: if the API key is missing or the call fails for any reason,
 *    this returns null and quiz scoring continues unaffected.
 *  - Mirrors the RestTemplate construction pattern already used in OpenTdbService.
 */
@Service
public class ExplanationService {

    private static final Logger log = LoggerFactory.getLogger(ExplanationService.class);

    private static final String API_URL = "https://api.anthropic.com/v1/messages";
    private static final String ANTHROPIC_VERSION = "2023-06-01";

    private static final String SYSTEM_PROMPT =
            "You are a friendly tutor helping a student learn from a quiz they just took. "
          + "Given a question and the verified correct answer, explain in 2-3 short sentences "
          + "why the correct answer is right, in plain language a high-school student understands. "
          + "Be encouraging and concise. Do not exceed about 60 words. "
          + "Do not mention that you are an AI.";

    private final RestTemplate restTemplate;
    private final String apiKey;
    private final String model;

    public ExplanationService(RestTemplateBuilder restTemplateBuilder,
                              @Value("${anthropic.api.key:}") String apiKey,
                              @Value("${anthropic.model:claude-haiku-4-5}") String model) {
        // Explicit timeouts so a slow API call can never hang the submit request thread.
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10_000); // 10 seconds
        factory.setReadTimeout(30_000);    // 30 seconds
        this.restTemplate = restTemplateBuilder
                .requestFactory(() -> factory)
                .build();
        this.apiKey = apiKey;
        this.model = model;
    }

    /**
     * Returns a learner-friendly explanation for one incorrect answer,
     * or null if generation is unavailable (missing key, API error, timeout, etc.).
     */
    public String generateExplanation(String questionText,
                                      List<String> options,
                                      String correctAnswer,
                                      String playerAnswer,
                                      String category) {

        if (apiKey == null || apiKey.isBlank()) {
            log.warn("ANTHROPIC_API_KEY is not set - skipping explanation generation.");
            return null;
        }

        String answerShown = (playerAnswer == null || playerAnswer.isBlank())
                ? "(left blank)" : playerAnswer;

        String userPrompt = "Category: " + category + "\n"
                + "Question: " + questionText + "\n"
                + "Options: " + String.join(", ", options) + "\n"
                + "Correct answer: " + correctAnswer + "\n"
                + "The student answered: " + answerShown + "\n\n"
                + "Explain why the correct answer is correct.";

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("x-api-key", apiKey);
            headers.set("anthropic-version", ANTHROPIC_VERSION);
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> body = new HashMap<>();
            body.put("model", model);
            body.put("max_tokens", 256);
            body.put("system", SYSTEM_PROMPT);
            body.put("messages", List.of(Map.of(
                    "role", "user",
                    "content", userPrompt)));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<JsonNode> response =
                    restTemplate.postForEntity(API_URL, entity, JsonNode.class);

            JsonNode responseBody = response.getBody();
            if (responseBody != null) {
                JsonNode content = responseBody.path("content");
                if (content.isArray() && content.size() > 0) {
                    String text = content.get(0).path("text").asText(null);
                    if (text != null && !text.isBlank()) {
                        return text.trim();
                    }
                }
            }
            log.warn("Claude API returned no usable explanation text.");
            return null;

        } catch (Exception e) {
            // Any failure (401, model_not_found, timeout, network) lands here.
            // We log and return null so the quiz submission still succeeds.
            log.error("Failed to generate explanation from Claude API: {}", e.getMessage());
            return null;
        }
    }
}