package com.fabiocondo.service.impl;

import com.fabiocondo.domain.*;
import com.fabiocondo.exception.domain.*;
import com.fabiocondo.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class PeriodicQuizService {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final PeriodicQuizRepository periodicQuizRepository;
    private final QuestionRepository questionRepository;
    private final TopicRepository topicRepository;

    public PeriodicQuizService(PeriodicQuizRepository periodicQuizRepository, QuestionRepository questionRepository, TopicRepository topicRepository) {
        this.periodicQuizRepository = periodicQuizRepository;
        this.questionRepository = questionRepository;
        this.topicRepository = topicRepository;
    }

    @Transactional
    public PeriodicQuiz createPeriodicQuiz(PeriodicQuiz periodicQuiz, Set<Long> topicIds) {

        if (topicIds == null || topicIds.isEmpty()) {
            throw new IllegalArgumentException("A Competition deve ter pelo menos um tópico associado.");
        }

        Set<Topic> topics = new HashSet<>(topicRepository.findAllById(topicIds));

        if (topics.size() != topicIds.size()) {
            throw new IllegalArgumentException("Um ou mais tópicos não foram encontrados no banco de dados.");
        }

        Set<Question> questions = questionRepository.findRandomQuestionsByTopicsAndDifficulty(topicIds, periodicQuiz.getDifficultyLevel(), 2);
        periodicQuiz.setQuestions(questions);
        periodicQuiz.setQuizId(UUID.randomUUID().toString());
        return periodicQuizRepository.save(periodicQuiz);
    }

    @Transactional
    public PeriodicQuiz updatePeriodicQuiz(Long id, PeriodicQuiz periodicQuiz, Set<Long> topicIds, Boolean generateQuestions) throws QuizNotFoundException {

        PeriodicQuiz existingPeriodicQuiz = getPeriodicQuizById(id);

        if(!existingPeriodicQuiz.getSubmissions().isEmpty()) {
            throw new IllegalArgumentException("A Competition não pode ser actualizada. Contém submissões.");
        }

        if (generateQuestions) {
            if (topicIds == null || topicIds.isEmpty()) {
                throw new IllegalArgumentException("A Competition deve ter pelo menos um tópico associado.");
            }

            Set<Topic> topics = new HashSet<>(topicRepository.findAllById(topicIds));

            if (topics.size() != topicIds.size()) {
                throw new IllegalArgumentException("Um ou mais tópicos não foram encontrados no banco de dados.");
            }

            existingPeriodicQuiz.getQuestions().clear();
            periodicQuizRepository.save(existingPeriodicQuiz); // Garantir que a remoção seja persistida

            Set<Question> questions = questionRepository.findRandomQuestionsByTopicsAndDifficulty(topicIds, periodicQuiz.getDifficultyLevel(), 2);
            existingPeriodicQuiz.setQuestions(questions);
            existingPeriodicQuiz.setDifficultyLevel(periodicQuiz.getDifficultyLevel());
        }

        BeanUtils.copyProperties(periodicQuiz, existingPeriodicQuiz, "id", "quizId", "questions", "submissions");

        return periodicQuizRepository.save(existingPeriodicQuiz);
    }

    public Page<PeriodicQuiz> findAll(String searchParam, Pageable pageable) {
        return periodicQuizRepository.findAll(searchParam, pageable);
    }

    //public Page<PeriodicQuiz> filter(PeriodicQuizFilter quizFilter, Pageable pageable) {
    //    return competitionRepository.filter(quizFilter, pageable);
    //}

    public PeriodicQuiz getPeriodicQuizById(Long id) throws QuizNotFoundException {
        return periodicQuizRepository.findById(id)
                .orElseThrow(() -> new QuizNotFoundException("No quiz found by id: " + id));
    }

    public PeriodicQuiz findPeriodicQuizByQuizId(String quizId) throws QuizNotFoundException {
        return periodicQuizRepository.findPeriodicQuizByQuizId(quizId)
                .orElseThrow(() -> new QuizNotFoundException("No quiz found by id: " + quizId));
    }

    public void delete(Long id) throws QuizNotFoundException {
        PeriodicQuiz existingPeriodicQuiz = getPeriodicQuizById(id);
        periodicQuizRepository.deleteById(existingPeriodicQuiz.getId());
    }

    public long getTotal(){
        return periodicQuizRepository.count();
    }

    public Set<Topic> getTopicsByCompetitionId(Long competitionId) {
        PeriodicQuiz periodicQuiz = periodicQuizRepository.findById(competitionId)
                .orElseThrow(() -> new RuntimeException("Competição não encontrada"));

        Set<Topic> topics = new HashSet<>();

        periodicQuiz.getQuestions().forEach(question -> {
            if (question.getTopic() != null) {
                topics.add(question.getTopic());
            }
        });

        return topics;
    }
}

