package com.fabiocondo.service.impl;

import com.fabiocondo.domain.Question;
import com.fabiocondo.dto.QuizQuestionStatisticsDTO;
import com.fabiocondo.repository.QuestionRepository;
import com.fabiocondo.repository.QuizRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class QuestionServiceStatistics {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final QuestionRepository questionRepository;
    private final QuizRepository quizRepository;

    public QuestionServiceStatistics(QuestionRepository questionRepository, QuizRepository quizRepository) {
        this.questionRepository = questionRepository;
        this.quizRepository = quizRepository;
    }

    public QuizQuestionStatisticsDTO getQuizStatisticsByQuestionId(Long questionId) {
        Optional<Question> optionalQuestion = questionRepository.findById(questionId);

        if (!optionalQuestion.isPresent()) {
            logger.error("Questão com ID {} não encontrada", questionId);
            return null;
        }

        Question question = optionalQuestion.get();

        // Contar as submissões corretas para a questão
        long correctAnswers = quizRepository.countByAnswers_IsCorrectTrueAndAnswers_Question(question);
        long totalAnswers = quizRepository.countByAnswers_Question(question);
        long nullAnswers = quizRepository.countByAnswers_IsNullAndAnswers_Question(question);
        long incorrectAnswers = totalAnswers - correctAnswers - nullAnswers;

        // Calcular as taxas de acerto e erro
        double accuracyRate = totalAnswers > 0 ? (double) correctAnswers / totalAnswers * 100 : 0;
        double errorRate = totalAnswers > 0 ? (double) (incorrectAnswers + nullAnswers) / totalAnswers * 100 : 0;

        // Contar quantas vezes a questão foi associada a competições
        long quizzesCount = quizRepository.countByQuestions(question);

        String topicName = question.getTopic() != null ? question.getTopic().getName() : "Sem Tópico";

        // Criar e retornar o DTO com as estatísticas da questão
        return new QuizQuestionStatisticsDTO(
                question.getId(),
                question.getText(),
                accuracyRate,
                errorRate,
                topicName,
                correctAnswers,
                incorrectAnswers + nullAnswers,
                totalAnswers,
                quizzesCount
        );
    }

}
