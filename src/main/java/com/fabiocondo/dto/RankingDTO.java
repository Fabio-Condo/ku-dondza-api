package com.fabiocondo.dto;

import java.util.Date;

public class RankingDTO {
    private String userName;
    private Long userId;
    private int score;
    private double accuracyRate;
    private Date submittedAt;
    private Integer position;

    // Construtor vazio
    public RankingDTO() {
    }

    // Construtor usado na query
    public RankingDTO(String userName, Long userId, long correctAnswers, long totalAnswers, Date submittedAt) {
        this.userName = userName;
        this.userId = userId;
        this.score = (int) correctAnswers;
        this.submittedAt = submittedAt;
        if (totalAnswers > 0) {
            this.accuracyRate = (double) correctAnswers / totalAnswers;
        } else {
            this.accuracyRate = 0.0;
        }
    }

    // getters e setters...
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public double getAccuracyRate() {
        return accuracyRate;
    }

    public void setAccuracyRate(double accuracyRate) {
        this.accuracyRate = accuracyRate;
    }

    public Date getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(Date submittedAt) {
        this.submittedAt = submittedAt;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }
}
