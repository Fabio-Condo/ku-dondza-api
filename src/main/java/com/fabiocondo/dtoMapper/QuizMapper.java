package com.fabiocondo.dtoMapper;

import com.fabiocondo.domain.Answer;
import com.fabiocondo.domain.Question;
import com.fabiocondo.domain.Quiz;
import com.fabiocondo.domain.Topic;
import com.fabiocondo.dto.QuizDTO;
import com.fabiocondo.repository.QuizRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class QuizMapper {

    private final QuizRepository quizRepository;

    public QuizMapper(QuizRepository quizRepository) {
        this.quizRepository = quizRepository;
    }

    // Converter DTO para Entidade (Quiz)
    public Quiz dtoToDomainObject(QuizDTO quizDTO) {
        Quiz quiz = new Quiz();
        quiz.setQuizId(quizDTO.getQuizId());
        quiz.setTitle(quizDTO.getTitle());
        quiz.setDifficultyLevel(quizDTO.getDifficultyLevel());
        quiz.setLimitPerTopic(quizDTO.getLimitPerTopic());
        quiz.setSubmittedAt(quizDTO.getSubmittedAt());
        quiz.setTimeSpent(quizDTO.getTimeSpent());
        quiz.setSubject(quizDTO.getSubject());
        quiz.setUser(quizDTO.getUser());
        quiz.setQuestions(quizDTO.getQuestions());
        quiz.setAnswers(quizDTO.getAnswers());
        return quiz;
    }

    // Converter Entidade (Quiz) para DTO
    public QuizDTO domainToDTO(Quiz quiz) {
        QuizDTO quizDTO = new QuizDTO();
        quizDTO.setId(quiz.getId());
        quizDTO.setQuizId(quiz.getQuizId());
        quizDTO.setTitle(quiz.getTitle());
        quizDTO.setDifficultyLevel(quiz.getDifficultyLevel());
        quizDTO.setLimitPerTopic(quiz.getLimitPerTopic());
        quizDTO.setSubmittedAt(quiz.getSubmittedAt());
        quizDTO.setTimeSpent(quiz.getTimeSpent());
        quizDTO.setSubject(quiz.getSubject());
        quizDTO.setUser(quiz.getUser());
        quizDTO.setTotalQuestions(quizRepository.countQuestionsByQuizId(quiz.getId()));

        // Topics
        Set<Topic> topics = new HashSet<>();
        quiz.getQuestions().forEach(question -> {
            if (question.getTopic() != null) {
                topics.add(question.getTopic());
            }
        });
        quizDTO.setTopics(topics);

        // Accuracy Rate - Taxa de acerto
        Set<Question> questions = quiz.getQuestions();
        int correctAnswers = 0;

        for (Question question : questions) {
            Set<Answer> userAnswers = quiz.getAnswers();

            for (Answer userAnswer : userAnswers) {
                if (userAnswer.getQuestion().equals(question) && userAnswer.isCorrect()) {
                    correctAnswers++;
                    break;
                }
            }
        }
        double accuracyRate = (double) correctAnswers / questions.size() * 100;
        quizDTO.setAccuracyRate(accuracyRate);

        return quizDTO;
    }

    // Converter Entidade (Quiz) para DTO
    public QuizDTO domainToDTO_WithQuestionsAndAnswers(Quiz quiz) {
        QuizDTO quizDTO = new QuizDTO();
        quizDTO.setId(quiz.getId());
        quizDTO.setQuizId(quiz.getQuizId());
        quizDTO.setTitle(quiz.getTitle());
        quizDTO.setDifficultyLevel(quiz.getDifficultyLevel());
        quizDTO.setLimitPerTopic(quiz.getLimitPerTopic());
        quizDTO.setSubmittedAt(quiz.getSubmittedAt());
        quizDTO.setTimeSpent(quiz.getTimeSpent());
        quizDTO.setSubject(quiz.getSubject());
        quizDTO.setUser(quiz.getUser());
        quizDTO.setQuestions(quiz.getQuestions()); //
        quizDTO.setAnswers(quiz.getAnswers());
        //quizDTO.setQuestions(quizRepository.findQuestionsByQuizId_v2(quiz.getId()));
        //quizDTO.setAnswers(quizRepository.findAnswersByQuizId(quiz.getId()));
        return quizDTO;
    }

    // Converter lista paginada de Quiz para DTO
    public Page<QuizDTO> domainPageToDTOPage(Page<Quiz> quizzes, Pageable pageable) {
        return new PageImpl<>(quizzes.stream()
                .map(this::domainToDTO)
                .collect(Collectors.toList()), pageable, quizzes.getTotalElements());
    }
}

