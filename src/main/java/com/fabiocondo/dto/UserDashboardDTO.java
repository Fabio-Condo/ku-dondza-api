package com.fabiocondo.dto;

public class UserDashboardDTO {

    private long totalQuizzesCreated;
    private long totalCompetitionsParticipating;


    public long getTotalQuizzesCreated() {
        return totalQuizzesCreated;
    }

    public void setTotalQuizzesCreated(long totalQuizzesCreated) {
        this.totalQuizzesCreated = totalQuizzesCreated;
    }

    public long getTotalCompetitionsParticipating() {
        return totalCompetitionsParticipating;
    }

    public void setTotalCompetitionsParticipating(long totalCompetitionsParticipating) {
        this.totalCompetitionsParticipating = totalCompetitionsParticipating;
    }
}

