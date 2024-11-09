package com.fabiocondo.service.impl;

import com.fabiocondo.domain.Question;
import com.fabiocondo.domain.Quiz;
import com.fabiocondo.exception.domain.QuestionNotFoundException;
import com.fabiocondo.exception.domain.QuizNotFoundException;
import com.fabiocondo.repository.QuestionRepository;
import com.fabiocondo.repository.QuizRepository;
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


    public QuizService(QuizRepository quizRepository, QuestionRepository questionRepository) {
        this.quizRepository = quizRepository;
        this.questionRepository = questionRepository;
    }

    public Quiz findById(Long id) throws QuizNotFoundException {
        logger.info("Getting quiz by id: " + id);
        return quizRepository.findById(id)
                .orElseThrow(() -> new QuizNotFoundException("No quiz found by id: " + id));
    }

    public Page<Quiz> findAll(String searchParam, Pageable pageable) {
        return quizRepository.findAll(searchParam, pageable);
    }

    public List<Quiz> findAll() {
        return quizRepository.findAll();
    }

    public Quiz save(Quiz quiz) {
        return quizRepository.save(quiz);
    }

    public Quiz update(Quiz quiz, Long id) throws QuizNotFoundException {
        Quiz existQuiz = findById(id);
        BeanUtils.copyProperties(quiz, existQuiz, "id");
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

    public Page<Question> getQuestionsByQuizId(Long quizId, Pageable pageable) throws QuizNotFoundException {
        Quiz quiz = findById(quizId);
        return quizRepository.findQuestionsByQuizId(quiz.getId(), pageable);
    }

    public Quiz addQuestionToQuiz(Long quizId, Long userId) throws QuestionNotFoundException, QuizNotFoundException {
        Quiz quiz = findById(quizId);
        Question question = questionRepository.findById(userId)
                .orElseThrow(() -> new QuestionNotFoundException("No question found by id: " + userId));
        quiz.getQuestions().add(question);
        return quizRepository.save(quiz);
    }

    public Quiz removeQuestionFromQuiz(Long quizId, Long userId) throws QuestionNotFoundException, QuizNotFoundException {
        Quiz quiz = findById(quizId);
        Question question = questionRepository.findById(userId)
                .orElseThrow(() -> new QuestionNotFoundException("No question found by id: " + userId));
        quiz.getQuestions().remove(question);
        return quizRepository.save(quiz);
    }

    public long countQuestionsByQuizId(Long competitionId){
        return quizRepository.countQuestionsByQuizId(competitionId);
    }

}
