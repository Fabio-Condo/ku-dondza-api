package com.fabiocondo.dto;

import com.fabiocondo.enumeration.DifficultyLevel;

import java.util.Date;

public class ChallengeSummaryDTO {

    private Long id;
    private String challengeId;

    private String title;
    private String description;

    private DifficultyLevel difficultyLevel;

    private Integer xpReward;

    private Date startDate;
    private Date endDate;

    // --- dados derivados (calculados) ---
    private Integer totalQuestions;

    private Integer remainingHours;

    private String status; // OPEN, ONGOING, DONE

    private Boolean submitted;

    private String subjectName;

    private Integer totalParticipants;

    private Integer averageScore;

    private Long currentUserRank;

    private double currentUserScore;

    // --- getters & setters ---

    public Long getId() {
        return id;
    }

    public String getChallengeId() {
        return challengeId;
    }

    public void setChallengeId(String challengeId) {
        this.challengeId = challengeId;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Integer getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(Integer totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public Integer getRemainingHours() {
        return remainingHours;
    }

    public void setRemainingHours(Integer remainingHours) {
        this.remainingHours = remainingHours;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Boolean getSubmitted() {
        return submitted;
    }

    public void setSubmitted(Boolean submitted) {
        this.submitted = submitted;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public Integer getTotalParticipants() {
        return totalParticipants;
    }

    public void setTotalParticipants(Integer totalParticipants) {
        this.totalParticipants = totalParticipants;
    }

    public Integer getAverageScore() {
        return averageScore;
    }

    public void setAverageScore(Integer averageScore) {
        this.averageScore = averageScore;
    }

    public Long getCurrentUserRank() {
        return currentUserRank;
    }

    public void setCurrentUserRank(Long currentUserRank) {
        this.currentUserRank = currentUserRank;
    }

    public double getCurrentUserScore() {
        return currentUserScore;
    }

    public void setCurrentUserScore(double currentUserScore) {
        this.currentUserScore = currentUserScore;
    }
}