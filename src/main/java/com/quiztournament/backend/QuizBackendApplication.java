package com.quiztournament.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class QuizBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(QuizBackendApplication.class, args);
    }
}
