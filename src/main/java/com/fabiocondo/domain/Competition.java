package com.fabiocondo.domain;

import com.fabiocondo.enumeration.CompetitionType;
import com.fabiocondo.enumeration.DifficultyLevel;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

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

    @Enumerated(EnumType.STRING)
    private CompetitionType competitionType;

    //@Column(nullable = false)
    private LocalDateTime expiry;

    private boolean active = false;

    @Enumerated(EnumType.STRING)
    private DifficultyLevel difficultyLevel;

    private int limitPerTopic;

    @ManyToOne
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @JsonIgnoreProperties({"competition"})
    @OneToMany(mappedBy = "competition", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Prize> prizes; // premios

    @JsonIgnore
    @ManyToMany
    @JoinTable(
            name = "competition_questions",
            joinColumns = @JoinColumn(name = "competition_id"),
            inverseJoinColumns = @JoinColumn(name = "question_id")
    )
    @OrderBy("id ASC")
    private Set<Question> questions = new HashSet<>();

    @JsonIgnore
    @ManyToMany
    @JoinTable(
            name = "competition_user",
            joinColumns = @JoinColumn(name = "competition_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    Set<User> allowedUsers = new HashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "competition", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<Submission> submissions;

    public Competition() {
    }

    public Competition(String competitionId, LocalDateTime expiry, boolean active, DifficultyLevel difficultyLevel, int limitPerTopic, Subject subject) {
        this.competitionId = competitionId;
        this.expiry = expiry;
        this.active = active;
        this.difficultyLevel = difficultyLevel;
        this.limitPerTopic = limitPerTopic;
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

    public CompetitionType getCompetitionType() {
        return competitionType;
    }

    public void setCompetitionType(CompetitionType competitionType) {
        this.competitionType = competitionType;
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

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public List<Prize> getPrizes() {
        return prizes;
    }

    public void setPrizes(List<Prize> prizes) {
        this.prizes = prizes;
    }

    public Set<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(Set<Question> questions) {
        this.questions = questions;
    }

    public Set<User> getAllowedUsers() {
        return allowedUsers;
    }

    public void setAllowedUsers(Set<User> allowedUsers) {
        this.allowedUsers = allowedUsers;
    }

    public List<Submission> getSubmissions() {
        return submissions;
    }

    public void setSubmissions(List<Submission> submissions) {
        this.submissions = submissions;
    }
}
