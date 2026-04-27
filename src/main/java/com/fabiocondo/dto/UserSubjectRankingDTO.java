package com.fabiocondo.dto;

public class UserSubjectRankingDTO {

    private Long userId;
    private String fullName;
    private String profileImageUrl;
    private Long score;
    private Long testsCompleted;
    private Double accuracyRate;

    public UserSubjectRankingDTO() {}

    public UserSubjectRankingDTO(Long userId,
                                 String fullName,
                                 String profileImageUrl,
                                 Long score,
                                 Long testsCompleted) {
        this.userId = userId;
        this.fullName = fullName;
        this.profileImageUrl = profileImageUrl;
        this.score = score;
        this.testsCompleted = testsCompleted;
    }

    public UserSubjectRankingDTO(Long userId, String fullName, String profileImageUrl, Long score) {
        this.userId = userId;
        this.fullName = fullName;
        this.profileImageUrl = profileImageUrl;
        this.score = score;
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

    public Long getScore() {
        return score;
    }

    public void setScore(Long score) {
        this.score = score;
    }

    public Long getTestsCompleted() {
        return testsCompleted;
    }

    public void setTestsCompleted(Long testsCompleted) {
        this.testsCompleted = testsCompleted;
    }

    public Double getAccuracyRate() {
        return accuracyRate;
    }

    public void setAccuracyRate(Double accuracyRate) {
        this.accuracyRate = accuracyRate;
    }
}
