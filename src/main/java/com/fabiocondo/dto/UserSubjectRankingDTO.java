package com.fabiocondo.dto;

public class UserSubjectRankingDTO {

    private Long userId;
    private String fullName;
    private String profileImageUrl;
    private Long score;
    private Double accuracyRate;

    public UserSubjectRankingDTO() {}

    public UserSubjectRankingDTO(Long userId,
                                 String fullName,
                                 String profileImageUrl,
                                 Long score,
                                 Double accuracyRate) {
        this.userId = userId;
        this.fullName = fullName;
        this.profileImageUrl = profileImageUrl;
        this.score = score;
        this.accuracyRate = accuracyRate;
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

    public Double getAccuracyRate() {
        return accuracyRate;
    }

    public void setAccuracyRate(Double accuracyRate) {
        this.accuracyRate = accuracyRate;
    }
}
