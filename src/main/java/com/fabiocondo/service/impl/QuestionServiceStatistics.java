package com.fabiocondo.service.impl;

import com.fabiocondo.domain.Question;
import com.fabiocondo.dto.CompetitionQuestionStatisticsDTO;
import com.fabiocondo.dto.QuizQuestionStatisticsDTO;
import com.fabiocondo.repository.CompetitionRepository;
import com.fabiocondo.repository.QuestionRepository;
import com.fabiocondo.repository.QuizRepository;
import com.fabiocondo.repository.SubmissionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class QuestionServiceStatistics {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final QuestionRepository questionRepository;
    private final QuizRepository quizRepository;
    private final SubmissionRepository submissionRepository;
    private final CompetitionRepository competitionRepository;

    public QuestionServiceStatistics(QuestionRepository questionRepository, QuizRepository quizRepository, SubmissionRepository submissionRepository, CompetitionRepository competitionRepository) {
        this.questionRepository = questionRepository;
        this.quizRepository = quizRepository;
        this.submissionRepository = submissionRepository;
        this.competitionRepository = competitionRepository;
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

    public CompetitionQuestionStatisticsDTO getCompetitionStatisticsByQuestionId(Long questionId) {
        Optional<Question> optionalQuestion = questionRepository.findById(questionId);

        if (!optionalQuestion.isPresent()) {
            logger.error("Questão com ID {} não encontrada", questionId);
            return null;
        }

        Question question = optionalQuestion.get();

        // Contar as submissões corretas para a questão
        long correctAnswers = submissionRepository.countByAnswers_IsCorrectTrueAndAnswers_Question(question);
        long totalAnswers = submissionRepository.countByAnswers_Question(question);
        long nullAnswers = submissionRepository.countByAnswers_IsNullAndAnswers_Question(question);
        long incorrectAnswers = totalAnswers - correctAnswers - nullAnswers;

        // Calcular as taxas de acerto e erro
        double accuracyRate = totalAnswers > 0 ? (double) correctAnswers / totalAnswers * 100 : 0;
        double errorRate = totalAnswers > 0 ? (double) (incorrectAnswers + nullAnswers) / totalAnswers * 100 : 0;

        // Contar quantas vezes a questão foi associada a competições
        long competitionsCount = competitionRepository.countByQuestions(question);

        String topicName = question.getTopic() != null ? question.getTopic().getName() : "Sem Tópico";

        // Criar e retornar o DTO com as estatísticas da questão
        return new CompetitionQuestionStatisticsDTO(
                question.getId(),
                question.getText(),
                accuracyRate,
                errorRate,
                topicName,
                correctAnswers,
                incorrectAnswers + nullAnswers,
                totalAnswers,
                competitionsCount
        );
    }

    // Método para calcular a taxa de acerto e erro de todas as questões
    //public List<CompetitionQuestionStatisticsDTO> getAllQuestionStatistics() {
    //    List<Question> questions = questionRepository.findAll();  // Busca todas as questões
    //    List<CompetitionQuestionStatisticsDTO> statistics = new ArrayList<>();

    //    for (Question question : questions) {
            // Contar as submissões corretas e incorretas para cada questão
    //        long correctAnswers = submissionRepository.countByAnswers_IsCorrectTrueAndAnswers_Question(question);
    //        long totalAnswers = submissionRepository.countByAnswers_Question(question);
    //        long nullAnswers = submissionRepository.countByAnswers_IsNullAndAnswers_Question(question);  // Contar respostas nulas
    //        long incorrectAnswers = totalAnswers - correctAnswers - nullAnswers;  // Subtrai os acertos e nulos para obter os incorretos

            // Calcular as taxas de acerto e erro
    //        double accuracyRate = totalAnswers > 0 ? (double) correctAnswers / totalAnswers * 100 : 0;
    //        double errorRate = totalAnswers > 0 ? (double) (incorrectAnswers + nullAnswers) / totalAnswers * 100 : 0;

    //        String topicName = question.getTopic() != null ? question.getTopic().getName() : "Sem Tópico";  // Caso não tenha tópico

            // Adicionar as estatísticas à lista
    //        statistics.add(new CompetitionQuestionStatisticsDTO(
    //                question.getId(),
    //                question.getText(),
    //                accuracyRate,
    //                errorRate,
    //                topicName,
    //                correctAnswers,
    //                incorrectAnswers + nullAnswers,
    //                totalAnswers,  // Adicionando o total de respostas, incluindo as nulas
    //                0
    //        ));
    //    }

    //    return statistics;
    //}

    //public Map<String, List<CompetitionQuestionStatisticsDTO>> getStatisticsByTopic() {
    //    List<CompetitionQuestionStatisticsDTO> statistics = getAllQuestionStatistics();
    //    Map<String, List<CompetitionQuestionStatisticsDTO>> statisticsByTopic = new HashMap<>();

    //    for (CompetitionQuestionStatisticsDTO dto : statistics) {
    //        statisticsByTopic
    //                .computeIfAbsent(dto.getTopicName(), k -> new ArrayList<>())
    //                .add(dto);
    //    }

    //    return statisticsByTopic;
    //}

}
