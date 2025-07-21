package com.fabiocondo.dtoMapper;

import com.fabiocondo.domain.Quiz;
import com.fabiocondo.domain.Topic;
import com.fabiocondo.dto.QuizDTO;
import com.fabiocondo.repository.QuizRepository;
import com.fabiocondo.service.impl.QuizService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class QuizMapper {

    private final QuizRepository quizRepository;
    private final QuizService quizService;

    public QuizMapper(QuizRepository quizRepository, QuizService quizService) {
        this.quizRepository = quizRepository;
        this.quizService = quizService;
    }

    public Quiz dtoToDomainObject(QuizDTO quizDTO) {
        Quiz quiz = new Quiz();
        quiz.setQuizId(quizDTO.getQuizId());
        quiz.setDifficultyLevel(quizDTO.getDifficultyLevel());
        quiz.setLimitPerTopic(quizDTO.getLimitPerTopic());
        quiz.setSubmittedAt(quizDTO.getSubmittedAt());
        quiz.setTimeLimit(quizDTO.getTimeLimit());
        quiz.setTimeSpent(quizDTO.getTimeSpent());
        quiz.setSubject(quizDTO.getSubject());
        quiz.setUser(quizDTO.getUser());
        quiz.setQuestions(quizDTO.getQuestions());
        quiz.setAnswers(quizDTO.getAnswers());
        return quiz;
    }

    public QuizDTO domainToDTO(Quiz quiz) {
        QuizDTO quizDTO = new QuizDTO();
        quizDTO.setId(quiz.getId());
        quizDTO.setQuizId(quiz.getQuizId());
        quizDTO.setDifficultyLevel(quiz.getDifficultyLevel());
        quizDTO.setLimitPerTopic(quiz.getLimitPerTopic());
        quizDTO.setSubmittedAt(quiz.getSubmittedAt());
        quizDTO.setTimeLimit(quiz.getTimeLimit());
        quizDTO.setTimeSpent(quiz.getTimeSpent());
        quizDTO.setSubject(quiz.getSubject());
        quizDTO.setUser(quiz.getUser());
        quizDTO.setTotalQuestions(quizRepository.countQuestionsByQuizId(quiz.getId()));
        quizDTO.setTopics(quizService.getSortedTopics(quiz));
        quizDTO.setAccuracyRate(quizService.calculateAccuracyRate(quiz));
        return quizDTO;
    }

    public QuizDTO domainToDTO_WithQuestionsAndAnswers(Quiz quiz) {
        QuizDTO quizDTO = new QuizDTO();
        quizDTO.setId(quiz.getId());
        quizDTO.setQuizId(quiz.getQuizId());
        quizDTO.setDifficultyLevel(quiz.getDifficultyLevel());
        quizDTO.setLimitPerTopic(quiz.getLimitPerTopic());
        quizDTO.setSubmittedAt(quiz.getSubmittedAt());
        quizDTO.setTimeLimit(quiz.getTimeLimit());
        quizDTO.setTimeSpent(quiz.getTimeSpent());
        quizDTO.setSubject(quiz.getSubject());
        quizDTO.setUser(quiz.getUser());
        quizDTO.setQuestions(quiz.getQuestions()); //
        quizDTO.setAnswers(quiz.getAnswers());
        return quizDTO;
    }

    public List<Topic> getTopics(QuizDTO quiz) {
        return quiz.getTopics()
                .stream()
                .sorted(Comparator.comparing(Topic::getName)) // ou getOrder(), getId(), etc.
                .collect(Collectors.toList());
    }

    public Page<QuizDTO> domainPageToDTOPage(Page<Quiz> quizzes, Pageable pageable) {
        return new PageImpl<>(quizzes.stream()
                .map(this::domainToDTO)
                .collect(Collectors.toList()), pageable, quizzes.getTotalElements());
    }
}

