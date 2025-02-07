package com.fabiocondo.dto;

public class CompetitionQuestionStatisticsDTO {

    private Long questionId;
    private String questionText;
    private double accuracyRate;
    private double errorRate;
    private String topicName;
    private long totalCorrectAnswers;
    private long totalIncorrectAnswers;
    private long totalAnswers;
    private long competitionsCount;

    public CompetitionQuestionStatisticsDTO(Long questionId, String questionText, double accuracyRate, double errorRate,
                                            String topicName, long totalCorrectAnswers, long totalIncorrectAnswers, long totalAnswers, long competitionsCount) {
        this.questionId = questionId;
        this.questionText = questionText;
        this.accuracyRate = accuracyRate;
        this.errorRate = errorRate;
        this.topicName = topicName;
        this.totalCorrectAnswers = totalCorrectAnswers;
        this.totalIncorrectAnswers = totalIncorrectAnswers;
        this.totalAnswers = totalAnswers;
        this.competitionsCount = competitionsCount;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public double getAccuracyRate() {
        return accuracyRate;
    }

    public void setAccuracyRate(double accuracyRate) {
        this.accuracyRate = accuracyRate;
    }

    public double getErrorRate() {
        return errorRate;
    }

    public void setErrorRate(double errorRate) {
        this.errorRate = errorRate;
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public long getTotalCorrectAnswers() {
        return totalCorrectAnswers;
    }

    public void setTotalCorrectAnswers(long totalCorrectAnswers) {
        this.totalCorrectAnswers = totalCorrectAnswers;
    }

    public long getTotalIncorrectAnswers() {
        return totalIncorrectAnswers;
    }

    public void setTotalIncorrectAnswers(long totalIncorrectAnswers) {
        this.totalIncorrectAnswers = totalIncorrectAnswers;
    }

    public long getTotalAnswers() {
        return totalAnswers;
    }

    public void setTotalAnswers(long totalAnswers) {
        this.totalAnswers = totalAnswers;
    }

    public long getCompetitionsCount() {
        return competitionsCount;
    }

    public void setCompetitionsCount(long competitionsCount) {
        this.competitionsCount = competitionsCount;
    }
}
