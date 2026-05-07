package com.fabiocondo.repository.filter;

import com.fabiocondo.domain.Subject;
import com.fabiocondo.enumeration.DifficultyLevel;

import javax.persistence.*;
import java.util.Date;

public class ChallengeFilter {

    @Column(unique = true, nullable = false)
    private String challengeId;

    private String title;

    private String description;

    private DifficultyLevel difficultyLevel;

    private Integer xpReward;

    private Date startDate;

    private Date endDate;

    private Subject subject;

    public String getChallengeId() {
        return challengeId;
    }

    public void setChallengeId(String challengeId) {
        this.challengeId = challengeId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public DifficultyLevel getDifficultyLevel() {
        return difficultyLevel;
    }

    public void setDifficultyLevel(DifficultyLevel difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }

    public Integer getXpReward() {
        return xpReward;
    }

    public void setXpReward(Integer xpReward) {
        this.xpReward = xpReward;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }
}
