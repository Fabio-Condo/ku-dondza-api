package com.fabiocondo.service.impl;

import com.fabiocondo.domain.Answer;
import com.fabiocondo.exception.domain.QuestionNotFoundException;
import com.fabiocondo.repository.AnswerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class AnswerServiceImpl { //aaa

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public AnswerRepository answerRepository;

    public AnswerServiceImpl(AnswerRepository answerRepository) {
        this.answerRepository = answerRepository;
    }

    public Answer findById(Long id) throws QuestionNotFoundException {
        logger.info("Getting exame by id: " + id);
        return answerRepository.findById(id)
                .orElseThrow(() -> new QuestionNotFoundException("No answer found by id: " + id));
    }

    public List<Answer> findAll() {
        return answerRepository.findAll();
    }

}
