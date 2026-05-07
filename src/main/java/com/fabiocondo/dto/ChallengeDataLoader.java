package com.fabiocondo.dto;

import com.fabiocondo.domain.Challenge;
import com.fabiocondo.domain.Subject;
import com.fabiocondo.enumeration.DifficultyLevel;
import com.fabiocondo.repository.ChallengeRepository;
import com.fabiocondo.repository.SubjectRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ChallengeDataLoader {

    @Bean
    CommandLineRunner initChallenges(
            ChallengeRepository challengeRepository,
            SubjectRepository subjectRepository) {

        return args -> {

            if (challengeRepository.count() > 0) return;

            // Subjects (assumindo já existentes)
            Subject math = subjectRepository.findById(1L).orElse(null);
            Subject physics = subjectRepository.findById(2L).orElse(null);
            Subject portuguese = subjectRepository.findById(3L).orElse(null);
            Subject chemistry = subjectRepository.findById(4L).orElse(null);
            Subject biology = subjectRepository.findById(5L).orElse(null);
            Subject history = subjectRepository.findById(6L).orElse(null);

            List<Challenge> challenges = new ArrayList<>();

            // =========================
            // 1. Matemática (UPCOMING)
            // =========================
            challenges.add(createChallenge(
                    "CH-MATH-01",
                    "Desafio Diário de Matemática",
                    "Matemática · Ciências Exactas",
                    DifficultyLevel.INTERMEDIATE
                    ,
                    25,
                    8,
                    math,
                    "UPCOMING"
            ));

            // =========================
            // 2. Física (ONGOING)
            // =========================
            challenges.add(createChallenge(
                    "CH-PHY-01",
                    "Quiz Relâmpago de Física",
                    "Física · Ciências Exactas",
                    DifficultyLevel.ADVANCED,
                    40,
                    1,
                    physics,
                    "ONGOING"
            ));

            // =========================
            // 3. Português (UPCOMING)
            // =========================
            challenges.add(createChallenge(
                    "CH-POR-01",
                    "Desafio de Português",
                    "Linguagens · Português",
                    DifficultyLevel.ADVANCED,
                    15,
                    6,
                    portuguese,
                    "UPCOMING"
            ));

            // =========================
            // 4. Química (UPCOMING)
            // =========================
            challenges.add(createChallenge(
                    "CH-QUIM-01",
                    "Liga de Química — Temporada 2",
                    "Química · Ciências Exactas",
                    DifficultyLevel.ADVANCED,
                    70,
                    9,
                    chemistry,
                    "UPCOMING"
            ));

            // =========================
            // 5. Biologia (DONE)
            // =========================
            challenges.add(createChallenge(
                    "CH-BIO-01",
                    "Copa de Biologia — Edição Abril",
                    "Biologia · Ciências Naturais",
                    DifficultyLevel.BEGINNER,
                    60,
                    -10,
                    biology,
                    "DONE"
            ));

            // =========================
            // 6. História (DONE)
            // =========================
            challenges.add(createChallenge(
                    "CH-HIS-01",
                    "Desafio de História de Moçambique",
                    "História · Ciências Sociais",
                    DifficultyLevel.INTERMEDIATE,
                    45,
                    -20,
                    history,
                    "DONE"
            ));

            challengeRepository.saveAll(challenges);

            System.out.println("✔ Challenges (UPCOMING / ONGOING / DONE) inseridos com sucesso!");
        };
    }

    private Challenge createChallenge(
            String id,
            String title,
            String description,
            DifficultyLevel difficulty,
            int xp,
            int daysOffset,
            Subject subject,
            String status
    ) {
        Challenge c = new Challenge();

        c.setChallengeId(id);
        c.setTitle(title);
        c.setDescription(description);
        c.setDifficultyLevel(difficulty);
        c.setXpReward(xp);

        Date now = new Date();
        c.setStartDate(now);

        // calcula data final (simples seed)
        c.setEndDate(new Date(System.currentTimeMillis() + (86400000L * daysOffset)));

        c.setSubject(subject);

        // 🔥 STATUS EXPLÍCITO (para frontend bater direto)
        //c.setStatus(status);

        c.setChallengeQuestions(new HashSet<>());
        c.setSubmittedChallengeQuizzes(new HashSet<>());

        return c;
    }
}