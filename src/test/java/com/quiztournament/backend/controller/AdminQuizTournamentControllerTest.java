package com.quiztournament.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quiztournament.backend.dto.QuizTournamentUpdateRequest;
import com.quiztournament.backend.entity.Question;
import com.quiztournament.backend.entity.QuizTournament;
import com.quiztournament.backend.entity.User;
import com.quiztournament.backend.repository.QuizTournamentRepository;
import com.quiztournament.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminQuizTournamentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private QuizTournamentRepository tournamentRepository;

    @Autowired
    private UserRepository userRepository;

    private QuizTournament savedTournament;

    @BeforeEach
    void setUp() {
        tournamentRepository.deleteAll();

        // Admin user is seeded by AdminUserInitializer
        User admin = userRepository.findByUsername("admin").orElseThrow();

        // Create a tournament directly in the DB (bypasses OpenTDB call)
        QuizTournament tournament = QuizTournament.builder()
                .name("Test Science Quiz")
                .category("Science")
                .categoryId(17)
                .difficulty("medium")
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(5))
                .minPassingScore(60)
                .totalQuestions(2)
                .createdAt(LocalDateTime.now())
                .createdBy(admin)
                .build();

        Question q1 = Question.builder()
                .type("multiple")
                .questionText("What is the chemical symbol for water?")
                .correctAnswer("H2O")
                .option1("H2O")
                .option2("CO2")
                .option3("NaCl")
                .option4("O2")
                .points(1)
                .quizTournament(tournament)
                .build();

        Question q2 = Question.builder()
                .type("boolean")
                .questionText("The Earth is flat.")
                .correctAnswer("False")
                .option1("True")
                .option2("False")
                .points(1)
                .quizTournament(tournament)
                .build();

        tournament.setQuestions(List.of(q1, q2));
        savedTournament = tournamentRepository.save(tournament);
    }

    @Test
    void getAllTournaments_ShouldReturnList() throws Exception {
        mockMvc.perform(get("/api/admin/tournaments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].name", is("Test Science Quiz")))
                .andExpect(jsonPath("$[0].status", is("ONGOING")));
    }

    @Test
    void getTournamentById_ShouldReturnTournamentWithQuestions() throws Exception {
        mockMvc.perform(get("/api/admin/tournaments/" + savedTournament.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Test Science Quiz")))
                .andExpect(jsonPath("$.questions", hasSize(2)))
                .andExpect(jsonPath("$.questions[0].correctAnswer").exists())
                .andExpect(jsonPath("$.questions[0].questionText").exists());
    }

    @Test
    void getTournamentById_ShouldReturn404ForInvalidId() throws Exception {
        mockMvc.perform(get("/api/admin/tournaments/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void updateTournament_ShouldUpdateFields() throws Exception {
        QuizTournamentUpdateRequest request = new QuizTournamentUpdateRequest();
        request.setName("Updated Quiz Name");
        request.setStartDate(LocalDateTime.now().minusDays(2));
        request.setEndDate(LocalDateTime.now().plusDays(10));

        mockMvc.perform(put("/api/admin/tournaments/" + savedTournament.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Updated Quiz Name")));
    }

    @Test
    void updateTournament_ShouldReturn400WhenEndDateBeforeStartDate() throws Exception {
        QuizTournamentUpdateRequest request = new QuizTournamentUpdateRequest();
        request.setName("Bad Dates");
        request.setStartDate(LocalDateTime.now().plusDays(10));
        request.setEndDate(LocalDateTime.now().plusDays(5));

        mockMvc.perform(put("/api/admin/tournaments/" + savedTournament.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteTournament_ShouldReturn200() throws Exception {
        mockMvc.perform(delete("/api/admin/tournaments/" + savedTournament.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Quiz tournament deleted successfully")));

        // Verify it's gone
        mockMvc.perform(get("/api/admin/tournaments/" + savedTournament.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteTournament_ShouldReturn404ForInvalidId() throws Exception {
        mockMvc.perform(delete("/api/admin/tournaments/9999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getTournamentAnalytics_ShouldReturnAnalytics() throws Exception {
        mockMvc.perform(get("/api/admin/tournaments/" + savedTournament.getId() + "/analytics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tournamentId", is(savedTournament.getId().intValue())))
                .andExpect(jsonPath("$.tournamentName", is("Test Science Quiz")))
                .andExpect(jsonPath("$.totalAttempts", is(0)))
                .andExpect(jsonPath("$.totalLikes", is(0)));
    }

    @Test
    void getTournamentLikes_ShouldReturnCount() throws Exception {
        mockMvc.perform(get("/api/admin/tournaments/" + savedTournament.getId() + "/likes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalLikes", is(0)));
    }

    @Test
    void getAllPlayers_ShouldReturnEmptyInitially() throws Exception {
        // Only admin exists initially, no players
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}
