package com.fabiocondo.dto;

import com.fabiocondo.domain.Question;
import com.fabiocondo.domain.Quiz;
import com.fabiocondo.domain.Topic;
import com.fabiocondo.enumeration.DifficultyLevel;
import com.fabiocondo.enumeration.TopicTestStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

public class TopicTestDTO {

    private Long id;

    @Enumerated(EnumType.STRING)
    private DifficultyLevel difficultyLevel;

    @JsonIgnoreProperties(
            value = { "questions", "contents", "hibernateLazyInitializer", "handler" }
    )// ADICIONANDO PORCAUSA DE TOPIC_TEST (MODULO DE PROGRESSAO) - MELHORAR DEPOIS COM DTO
    private Topic topic;

    @OrderBy("id ASC")
    private Set<Question> questions = new HashSet<>();

    private Set<Quiz> submittedQuizzes = new HashSet<>();

    private int orderIndex; // 1, 2, 3

    @Enumerated(EnumType.STRING)
    private TopicTestStatus topicTestStatus;

    private double accuracyRate;

    private Long totalQuestions;

    public TopicTestDTO() {
    }

    public TopicTestDTO(Long id, DifficultyLevel difficultyLevel, Topic topic, Set<Question> questions, Set<Quiz> submittedQuizzes, int orderIndex, TopicTestStatus topicTestStatus) {
        this.id = id;
        this.difficultyLevel = difficultyLevel;
        this.topic = topic;
        this.questions = questions;
        this.submittedQuizzes = submittedQuizzes;
        this.orderIndex = orderIndex;
        this.topicTestStatus = topicTestStatus;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public DifficultyLevel getDifficultyLevel() {
        return difficultyLevel;
    }

    public void setDifficultyLevel(DifficultyLevel difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }

    public Topic getTopic() {
        return topic;
    }

    public void setTopic(Topic topic) {
        this.topic = topic;
    }

    public Set<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(Set<Question> questions) {
        this.questions = questions;
    }

    public Set<Quiz> getSubmittedQuizzes() {
        return submittedQuizzes;
    }

    public void setSubmittedQuizzes(Set<Quiz> submittedQuizzes) {
        this.submittedQuizzes = submittedQuizzes;
    }

    public int getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
    }

    public TopicTestStatus getTopicTestStatus() {
        return topicTestStatus;
    }

    public void setTopicTestStatus(TopicTestStatus topicTestStatus) {
        this.topicTestStatus = topicTestStatus;
    }

    public double getAccuracyRate() {
        return accuracyRate;
    }

    public void setAccuracyRate(double accuracyRate) {
        this.accuracyRate = accuracyRate;
    }

    public Long getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(Long totalQuestions) {
        this.totalQuestions = totalQuestions;
    }
}
