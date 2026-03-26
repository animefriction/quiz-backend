package com.quiztournament.backend.service;

import com.quiztournament.backend.entity.QuizLike;
import com.quiztournament.backend.entity.QuizTournament;
import com.quiztournament.backend.entity.User;
import com.quiztournament.backend.exception.ResourceNotFoundException;
import com.quiztournament.backend.repository.QuizLikeRepository;
import com.quiztournament.backend.repository.QuizTournamentRepository;
import com.quiztournament.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class QuizLikeService {

    private final QuizLikeRepository quizLikeRepository;
    private final QuizTournamentRepository quizTournamentRepository;
    private final UserRepository userRepository;

    public int likeTournament(Long userId, Long tournamentId) {
        if (quizLikeRepository.existsByUserIdAndQuizTournamentId(userId, tournamentId)) {
            throw new IllegalArgumentException("You have already liked this tournament");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        QuizTournament tournament = quizTournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Quiz tournament not found with id: " + tournamentId));

        QuizLike like = QuizLike.builder()
                .user(user)
                .quizTournament(tournament)
                .likedAt(LocalDateTime.now())
                .build();

        quizLikeRepository.save(like);
        return (int) quizLikeRepository.countByQuizTournamentId(tournamentId);
    }

    public int unlikeTournament(Long userId, Long tournamentId) {
        QuizLike like = quizLikeRepository.findByUserIdAndQuizTournamentId(userId, tournamentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Like not found for this user and tournament"));

        quizLikeRepository.delete(like);
        return (int) quizLikeRepository.countByQuizTournamentId(tournamentId);
    }
}
