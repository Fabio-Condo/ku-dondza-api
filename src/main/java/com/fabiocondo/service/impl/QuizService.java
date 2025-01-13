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


    public QuizService(QuizRepository quizRepository, QuestionRepository questionRepository, UserRepository userRepository) {
        this.quizRepository = quizRepository;
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
        quiz.setQuestions(quiz.getQuestions());
        Quiz saveQuiz = quizRepository.save(quiz);
        return quizRepository.save(saveQuiz);
        //return quizRepository.save(quiz);
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

    public Page<Question> getQuestionsByQuizId(Long quizId, Pageable pageable) throws QuizNotFoundException {
        //try {
        //    Thread.sleep(3000);
        //} catch (InterruptedException e) {
        //    Thread.currentThread().interrupt();
        //    throw new RuntimeException("A operação foi interrompida", e);
        //}

        Quiz quiz = findById(quizId);
        return quizRepository.findQuestionsByQuizId(quiz.getId(), pageable);
    }

    public long countQuestionsByQuizId(Long quizId){
        return quizRepository.countQuestionsByQuizId(quizId);
    }

    private String generateQuizId() {
        return RandomStringUtils.randomAlphanumeric(10);
    }

}
