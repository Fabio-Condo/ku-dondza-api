package com.fabiocondo.service.impl;

import com.fabiocondo.domain.Question;
import com.fabiocondo.domain.Quiz;
import com.fabiocondo.domain.User;
import com.fabiocondo.exception.domain.QuestionNotFoundException;
import com.fabiocondo.exception.domain.QuizNotFoundException;
import com.fabiocondo.exception.domain.UserNotFoundException;
import com.fabiocondo.repository.QuestionRepository;
import com.fabiocondo.repository.QuizRepository;
import com.fabiocondo.repository.UserRepository;
import com.fabiocondo.repository.filter.QuizFilter;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuizService {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;


    public QuizService(QuizRepository quizRepository, QuestionRepository questionRepository, UserRepository userRepository) {
        this.quizRepository = quizRepository;
        this.questionRepository = questionRepository;
        this.userRepository = userRepository;
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

    public Page<Quiz> findAll(String searchParam, Pageable pageable) {
        return quizRepository.findAll(searchParam, pageable);
    }

    public Page<Quiz> filter(QuizFilter quizFilter, Pageable pageable) {
        return quizRepository.filter(quizFilter, pageable);
    }

    public List<Quiz> findAll() {
        return quizRepository.findAll();
    }

    public Quiz save(Quiz quiz) {
        quiz.setQuizId(generateQuizId());
        return quizRepository.save(quiz);
    }

    public Quiz update(Quiz quiz, Long id) throws QuizNotFoundException {
        Quiz existQuiz = findById(id);
        BeanUtils.copyProperties(quiz, existQuiz, "id", "quizId", "questions");
        logger.info("Updating quiz: " + quiz.getTitle());
        return quizRepository.save(existQuiz);
    }

    public void delete(Long id) throws QuizNotFoundException {
        Quiz existQuiz = findById(id);
        logger.info("Deleting quiz: " + existQuiz.getTitle());
        quizRepository.deleteById(id);
    }

    public long getTotal(){
        logger.info("Total quizzes: " + quizRepository.count());
        return quizRepository.count();
    }

    public Page<Question> getQuestionsByQuizIdAndUserId(Long quizId, Long userId, Pageable pageable) throws QuizNotFoundException, UserNotFoundException {
        Quiz quiz = findById(quizId);
        User user = userRepository.findById(userId).
                orElseThrow(() -> new UserNotFoundException("No user found by id: " + userId));
        return quizRepository.getQuestionsByQuizIdAndUserId(quiz.getId(), user.getId(), pageable);
    }

    public Quiz addQuestionToQuiz(Long quizId, Long questionId) throws QuestionNotFoundException, QuizNotFoundException {
        Quiz quiz = findById(quizId);
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new QuestionNotFoundException("No question found by id: " + questionId));
        quiz.getQuestions().add(question);
        return quizRepository.save(quiz);
    }

    public Quiz removeQuestionFromQuiz(Long quizId, Long questionId) throws QuestionNotFoundException, QuizNotFoundException {
        Quiz quiz = findById(quizId);
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new QuestionNotFoundException("No question found by id: " + questionId));
        quiz.getQuestions().remove(question);
        return quizRepository.save(quiz);
    }

    public long countQuestionsByQuizId(Long quizId){
        return quizRepository.countQuestionsByQuizId(quizId);
    }

    private String generateQuizId() {
        return RandomStringUtils.randomAlphanumeric(10);
    }

}
