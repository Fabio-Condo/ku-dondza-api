package com.fabiocondo.service.impl;

import com.fabiocondo.domain.Question;
import com.fabiocondo.exception.domain.QuestionNotFoundException;
import com.fabiocondo.repository.QuestionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Service
public class QuestionService {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final QuestionRepository questionRepository;

    public QuestionService(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    public Question findById(Long id) throws QuestionNotFoundException {
        logger.info("Getting question by id: " + id);
        return questionRepository.findById(id)
                .orElseThrow(() -> new QuestionNotFoundException("No question found by id: " + id));
    }

    public Page<Question> findAll(Pageable pageable) {
        return questionRepository.findAll(pageable);
    }

    public List<Question> findAll() {
        return questionRepository.findAll();
    }

    public Question save(Question question) {
        question.getAnswers().forEach(answer -> answer.setQuestion(question));
        logger.info("Saving question: " + question.getText());
        return questionRepository.save(question);
    }

    public Question update(Question question, Long id) throws QuestionNotFoundException {
        Question existQuestion = findById(id);

        existQuestion.getAnswers().clear();
        existQuestion.getAnswers().addAll(question.getAnswers());
        existQuestion.getAnswers().forEach(answer -> answer.setQuestion(existQuestion));

        BeanUtils.copyProperties(question, existQuestion, "answers");
        logger.info("Updating question: " + existQuestion.getText());
        return questionRepository.save(existQuestion);
    }

    public void delete(Long id) throws QuestionNotFoundException {
        Question existQuestion = findById(id);
        logger.info("Deleting quiz: " + existQuestion.getText());
        questionRepository.deleteById(id);
    }

    public Page<Question> findByQuizId(Long quizId, Pageable pageable) {
        return questionRepository.findByQuizId(quizId, pageable);
    }

    public long getTotal(){
        logger.info("Total quizzes: " + questionRepository.count());
        return questionRepository.count();
    }
}
