package com.fabiocondo.dto;

public class UserSubjectRankingSummaryDTO {

    private String subjectId;
    private String subjectName;

    private Long userId;
    private String fullName;
    private String profileImageUrl;

    private Long currentUserRank;
    private double currentUserScore;

    private double accuracyRate;
    private double averageScore;
    private long totalTopics;
    private Long totalTests;

    public UserSubjectRankingSummaryDTO() {
    }

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
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

    public double getAccuracyRate() {
        return accuracyRate;
    }

    public void setAccuracyRate(double accuracyRate) {
        this.accuracyRate = accuracyRate;
    }

    public double getAverageScore() {
        return averageScore;
    }

    public void setAverageScore(double averageScore) {
        this.averageScore = averageScore;
    }

    public long getTotalTopics() {
        return totalTopics;
    }

    public void setTotalTopics(long totalTopics) {
        this.totalTopics = totalTopics;
    }

    public Long getTotalTests() {
        return totalTests;
    }

    public void setTotalTests(Long totalTests) {
        this.totalTests = totalTests;
    }
}
