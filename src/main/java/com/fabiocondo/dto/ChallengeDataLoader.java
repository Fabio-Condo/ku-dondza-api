package com.fabiocondo.dto;

import com.fabiocondo.domain.Challenge;
import com.fabiocondo.domain.Question;
import com.fabiocondo.domain.Subject;
import com.fabiocondo.enumeration.DifficultyLevel;
import com.fabiocondo.repository.ChallengeRepository;
import com.fabiocondo.repository.QuestionRepository;
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
            SubjectRepository subjectRepository,
            QuestionRepository questionRepository
    ) {

        return args -> {

            if (challengeRepository.count() > 0) {
                return;
            }

            List<Subject> subjects = subjectRepository.findAll();

            List<Challenge> challenges = new ArrayList<>();

            for (Subject subject : subjects) {

                if (subject == null) continue;

                challenges.add(createChallengeForSubject(subject, questionRepository));
            }

            challengeRepository.saveAll(challenges);

            System.out.println("✔ Challenges gerados automaticamente para todos os subjects!");
        };
    }

    private Challenge createChallengeForSubject(
            Subject subject,
            QuestionRepository questionRepository
    ) {

        Challenge challenge = new Challenge();

        String code = "CH-" + subject.getId();

        challenge.setChallengeId(code);
        challenge.setTitle("Desafio de " + subject.getName());
        challenge.setDescription(subject.getName() + " · Preparação de Exame");

        challenge.setDifficultyLevel(randomDifficulty());
        challenge.setXpReward(randomXP());

        Date now = new Date();
        challenge.setStartDate(now);

        // duração aleatória entre 1 e 10 dias
        int daysOffset = new Random().nextInt(10) + 1;

        challenge.setEndDate(
                new Date(System.currentTimeMillis() + (86400000L * daysOffset))
        );

        challenge.setSubject(subject);

        // Buscar questões via topic -> subject
        List<Question> questions =
                questionRepository.findByTopicSubjectId(subject.getId());

        if (questions.isEmpty()) {
            challenge.setChallengeQuestions(new HashSet<>());
            challenge.setSubmittedChallengeQuizzes(new HashSet<>());
            return challenge;
        }

        Collections.shuffle(questions);

        // quantidade variável (entre 5 e 12)
        int questionLimit = new Random().nextInt(8) + 5;

        int totalQuestions = Math.min(questions.size(), questionLimit);

        List<Question> selected = questions.subList(0, totalQuestions);

        challenge.setChallengeQuestions(new HashSet<>(selected));
        challenge.setSubmittedChallengeQuizzes(new HashSet<>());

        return challenge;
    }

    private DifficultyLevel randomDifficulty() {
        DifficultyLevel[] levels = DifficultyLevel.values();
        return levels[new Random().nextInt(levels.length)];
    }

    private int randomXP() {
        return 10 + new Random().nextInt(90); // 10 a 100 XP
    }
}