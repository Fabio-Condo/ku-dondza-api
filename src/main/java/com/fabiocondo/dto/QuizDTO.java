package com.fabiocondo.dto;

import com.fabiocondo.domain.*;
import com.fabiocondo.enumeration.QuizType;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class QuizDTO {

    private Long id;

    private String quizId;

    @Enumerated(EnumType.STRING)
    private QuizType type;

    private int limitPerTopic;

    @Temporal(TemporalType.TIMESTAMP)
    private Date submittedAt;

    private Integer timeLimit; // Tempo atribuído em segundos

    private Integer timeSpent; // Tempo gasto em segundos

    @Column(nullable = false)
    private boolean anonymous = false;

    private SubjectDto subject;

    private UserDTO user;

    //private Set<Question> questions = new HashSet<>();
    private Set<QuestionDTO> questions = new HashSet<>();

    private Set<Answer> answers = new HashSet<>();

    private Set<TopicDTO> topics = new HashSet<>();

    private Long totalQuestions;

    private double accuracyRate;


    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getQuizId() {
        return quizId;
    }

    public void setQuizId(String quizId) {
        this.quizId = quizId;
    }

    public QuizType getType() {
        return type;
    }

    public void setType(QuizType type) {
        this.type = type;
    }

    public int getLimitPerTopic() {
        return limitPerTopic;
    }

    public void setLimitPerTopic(int limitPerTopic) {
        this.limitPerTopic = limitPerTopic;
    }

    public Date getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(Date submittedAt) {
        this.submittedAt = submittedAt;
    }

    public Integer getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(Integer timeLimit) {
        this.timeLimit = timeLimit;
    }

    public Integer getTimeSpent() {
        return timeSpent;
    }

    public void setTimeSpent(Integer timeSpent) {
        this.timeSpent = timeSpent;
    }

    public boolean isAnonymous() {
        return anonymous;
    }

    public void setAnonymous(boolean anonymous) {
        this.anonymous = anonymous;
    }

    public SubjectDto getSubject() { return subject; }

    public void setSubject(SubjectDto subject) { this.subject = subject; }

    public UserDTO getUser() { return user; }

    public void setUser(UserDTO user) { this.user = user; }

    public Set<QuestionDTO> getQuestions() {
        return questions;
    }

    public void setQuestions(Set<QuestionDTO> questions) {
        this.questions = questions;
    }

    public Set<Answer> getAnswers() {
        return answers;
    }

    public void setAnswers(Set<Answer> answers) {
        this.answers = answers;
    }

    public Set<TopicDTO> getTopics() {
        return topics;
    }

    public void setTopics(Set<TopicDTO> topics) {
        this.topics = topics;
    }

    public Long getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(Long totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public double getAccuracyRate() {
        return accuracyRate;
    }

    public void setAccuracyRate(double accuracyRate) {
        this.accuracyRate = accuracyRate;
    }

// Métodos para calcular total de acertos e erros considerando as respostas fornecidas
    //public int getTotalAcertos() {
    //    int acertos = 0;
        // Percorre as perguntas e verifica se as respostas fornecidas pelo usuário estão corretas
    //    for (Question question : questions) {
    //        for (Answer answer : question.getAnswers()) {
    //            if (answer.isCorrect() && this.answers.contains(answer)) {
    //                acertos++;
    //            }
    //        }
    //    }
    //    return acertos;
    //}

    //public int getTotalErros() {
    //    int erros = 0;
        // Percorre as perguntas e verifica se as respostas fornecidas estão incorretas
    //    for (Question question : questions) {
    //        for (Answer answer : question.getAnswers()) {
    //            if (!answer.isCorrect() && this.answers.contains(answer)) {
    //                erros++;
    //            }
    //        }
    //    }
    //    return erros;
    //}
}

