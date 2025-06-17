package com.fabiocondo.dto;

import com.fabiocondo.domain.Prize;
import com.fabiocondo.domain.Question;
import com.fabiocondo.domain.Subject;
import com.fabiocondo.domain.Topic;
import com.fabiocondo.enumeration.CompetitionType;
import com.fabiocondo.enumeration.DifficultyLevel;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CompetitionDto {

    private Long id;

    private String competitionId;

    private LocalDateTime expiry;

    private boolean active = false;

    private boolean isOpen;

    @Enumerated(EnumType.STRING)
    private CompetitionType competitionType;

    @Enumerated(EnumType.STRING)
    private DifficultyLevel difficultyLevel;

    private int limitPerTopic;

    private Subject subject;

    private Long totalTopics;

    private Long totalQuestions;

    private Long totalSubmissions;

    private boolean currentUserAllowedToSubmit ;

    private boolean currentUserHasSubmitted ;

    private Set<Topic> topics;

    private Set<Question> questions = new HashSet<>();

    private List<Prize> prizes = new ArrayList<>();

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

    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean open) {
        this.isOpen = open;
    }

    public DifficultyLevel getDifficultyLevel() {
        return difficultyLevel;
    }

    public void setDifficultyLevel(DifficultyLevel difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }

    public CompetitionType getCompetitionType() {
        return competitionType;
    }

    public void setCompetitionType(CompetitionType competitionType) {
        this.competitionType = competitionType;
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

    public Long getTotalTopics() {
        return totalTopics;
    }

    public void setTotalTopics(Long totalTopics) {
        this.totalTopics = totalTopics;
    }

    public Long getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(Long totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public Long getTotalSubmissions() {
        return totalSubmissions;
    }

    public boolean isCurrentUserAllowedToSubmit() {
        return currentUserAllowedToSubmit;
    }

    public void setCurrentUserAllowedToSubmit(boolean currentUserAllowedToSubmit) {
        this.currentUserAllowedToSubmit = currentUserAllowedToSubmit;
    }

    public boolean isCurrentUserHasSubmitted() {
        return currentUserHasSubmitted;
    }

    public void setCurrentUserHasSubmitted(boolean currentUserHasSubmitted) {
        this.currentUserHasSubmitted = currentUserHasSubmitted;
    }

    public void setTotalSubmissions(Long totalSubmissions) {
        this.totalSubmissions = totalSubmissions;
    }

    public Set<Topic> getTopics() {
        return topics;
    }

    public void setTopics(Set<Topic> topics) {
        this.topics = topics;
    }

    public Set<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(Set<Question> questions) {
        this.questions = questions;
    }

    public List<Prize> getPrizes() {
        return prizes;
    }

    public void setPrizes(List<Prize> prizes) {
        this.prizes = prizes;
    }
}
