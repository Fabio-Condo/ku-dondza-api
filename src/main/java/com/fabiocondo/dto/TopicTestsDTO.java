package com.fabiocondo.dto;

import java.util.List;

public class TopicTestsDTO {
    private Long topicId;
    private String topicName;
    private List<TestDTO> tests;
    private double progressRate;
    private boolean completed;

    public TopicTestsDTO() {}

    public TopicTestsDTO(Long topicId, String topicName, List<TestDTO> tests) {
        this.topicId = topicId;
        this.topicName = topicName;
        this.tests = tests;
    }

    public Long getTopicId() { return topicId; }
    public void setTopicId(Long topicId) { this.topicId = topicId; }

    public String getTopicName() { return topicName; }
    public void setTopicName(String topicName) { this.topicName = topicName; }

    public List<TestDTO> getTests() { return tests; }
    public void setTests(List<TestDTO> tests) { this.tests = tests; }

    public double getProgressRate() {
        return progressRate;
    }

    public void setProgressRate(double progressRate) {
        this.progressRate = progressRate;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}

