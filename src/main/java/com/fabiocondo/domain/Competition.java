package com.fabiocondo.domain;

import com.fabiocondo.enumeration.DifficultyLevel;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "competition")
public class Competition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String competitionId;

    private String title;

    //@Column(nullable = false)
    private LocalDateTime expiry;

    private boolean active = false;

    @Enumerated(EnumType.STRING)
    private DifficultyLevel difficultyLevel;

    private int limitPerTopic;

    private Integer timeLimit; // Tempo atribuído em segundos

    private Integer timeSpent; // Tempo gasto em segundos

    @ManyToOne
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @JsonIgnore
    @ManyToMany
    @JoinTable(
            name = "competition_questions",
            joinColumns = @JoinColumn(name = "competition_id"),
            inverseJoinColumns = @JoinColumn(name = "question_id")
    )
    private Set<Question> questions = new HashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "competition", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<Submission> submissions;

    public Competition() {
    }

    public Competition(String competitionId, String title, LocalDateTime expiry, boolean active, DifficultyLevel difficultyLevel, int limitPerTopic, Integer timeLimit, Integer timeSpent, Subject subject) {
        this.competitionId = competitionId;
        this.title = title;
        this.expiry = expiry;
        this.active = active;
        this.difficultyLevel = difficultyLevel;
        this.limitPerTopic = limitPerTopic;
        this.timeLimit = timeLimit;
        this.timeSpent = timeSpent;
        this.subject = subject;
    }

    public boolean isOpen() {
        return active && expiry.isAfter(LocalDateTime.now());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCompetitionId() {
        return competitionId;
    }

    public void setCompetitionId(String competitionId) {
        this.competitionId = competitionId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDateTime getExpiry() {
        return expiry;
    }

    public void setExpiry(LocalDateTime expiry) {
        this.expiry = expiry;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public DifficultyLevel getDifficultyLevel() {
        return difficultyLevel;
    }

    public void setDifficultyLevel(DifficultyLevel difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }

    public int getLimitPerTopic() {
        return limitPerTopic;
    }

    public void setLimitPerTopic(int limitPerTopic) {
        this.limitPerTopic = limitPerTopic;
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

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public Set<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(Set<Question> questions) {
        this.questions = questions;
    }

    public List<Submission> getSubmissions() {
        return submissions;
    }

    public void setSubmissions(List<Submission> submissions) {
        this.submissions = submissions;
    }
}
