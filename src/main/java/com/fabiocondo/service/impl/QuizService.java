package com.fabiocondo.service.impl;

import com.fabiocondo.domain.Answer;
import com.fabiocondo.domain.Question;
import com.fabiocondo.domain.Quiz;
import com.fabiocondo.domain.Topic;
import com.fabiocondo.exception.domain.QuizNotFoundException;
import com.fabiocondo.repository.AnswerRepository;
import com.fabiocondo.repository.QuestionRepository;
import com.fabiocondo.repository.QuizRepository;
import com.fabiocondo.repository.filter.QuizFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class QuizService {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;


    public QuizService(QuizRepository quizRepository, QuestionRepository questionRepository, AnswerRepository answerRepository) {
        this.quizRepository = quizRepository;
        this.questionRepository = questionRepository;
        this.answerRepository = answerRepository;
    }

    public Quiz findById(Long id) throws QuizNotFoundException {
        logger.info("Getting quiz by id: " + id);
        return quizRepository.findById(id)
                .orElseThrow(() -> new QuizNotFoundException("No quiz found by id: " + id));
    }

    public Quiz findQuizByQuizId(String quizId) throws QuizNotFoundException {
        return quizRepository.findQuizByQuizId(quizId)
                .orElseThrow(() -> new QuizNotFoundException("No quiz found by id: " + quizId));
    }

    public Page<Quiz> filter(QuizFilter quizFilter, Pageable pageable) {
        return quizRepository.filter(quizFilter, pageable);
    }

    public Page<Quiz> getQuizzesByQuestionId(Long questionId, Pageable pageable) throws QuizNotFoundException {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new QuizNotFoundException("No question found by id: " + questionId));
        return quizRepository.findAllByQuestions(question, pageable);
    }

    @Transactional
    public Quiz saveQuizWithQuestions(Quiz quiz, Set<Long> questionIds, Set<Long> userAnswerIds) {

        if (quiz == null) {
            throw new IllegalArgumentException("O objeto Quiz não pode ser nulo.");
        }

        if (questionIds == null || questionIds.isEmpty()) {
            throw new IllegalArgumentException("O Quiz deve ter pelo menos uma questão associada.");
        }

        Set<Question> questions = new HashSet<>(questionRepository.findAllById(questionIds));
        Set<Answer> answers = new HashSet<>(answerRepository.findAllById(userAnswerIds));

        if (questions.size() != questionIds.size()) {
            throw new IllegalArgumentException("Uma ou mais questões não foram encontradas no banco de dados.");
        }

        quiz.setQuizId(UUID.randomUUID().toString());
        quiz.setSubmittedAt(new Date());
        quiz.setQuestions(questions);
        quiz.setAnswers(answers);

        return quizRepository.save(quiz);
    }

    public void delete(Long id) throws QuizNotFoundException {
        Quiz existQuiz = findById(id);
        quizRepository.deleteById(id);
    }

    public long countByUserId(Long userId){
        return quizRepository.countByUserId(userId);
    }

    public long countQuestionsByQuizId(Long quizId){
        return quizRepository.countQuestionsByQuizId(quizId);
    }

    public Set<Topic> getTopics(Quiz quiz) {
        Set<Topic> topics = new HashSet<>();
        quiz.getQuestions().forEach(question -> {
            if (question.getTopic() != null) {
                topics.add(question.getTopic());
            }
        });
        return topics;
    }

    public double calculateAccuracyRate(Quiz quiz) {
        Set<Question> questions = quiz.getQuestions();
        Set<Answer> userAnswers = quiz.getAnswers();
        int correctAnswers = 0;

        for (Question question : questions) {
            for (Answer userAnswer : userAnswers) {
                if (userAnswer.getQuestion().equals(question) && userAnswer.isCorrect()) {
                    correctAnswers++;
                    break; // encontrou a resposta correta para essa questão
                }
            }
        }

        if (questions.isEmpty()) {
            return 0.0;
        }

        return (double) correctAnswers / questions.size() * 100;
    }

}
