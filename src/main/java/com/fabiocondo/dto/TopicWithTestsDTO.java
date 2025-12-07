package com.fabiocondo.dto;

import java.util.List;

public class TopicWithTestsDTO {
    private Long topicId;
    private String topicName;
    private List<TopicTestDTO> tests;

    public TopicWithTestsDTO() {}

    public TopicWithTestsDTO(Long topicId, String topicName, List<TopicTestDTO> tests) {
        this.topicId = topicId;
        this.topicName = topicName;
        this.tests = tests;
    }

    public Long getTopicId() { return topicId; }
    public void setTopicId(Long topicId) { this.topicId = topicId; }

    public String getTopicName() { return topicName; }
    public void setTopicName(String topicName) { this.topicName = topicName; }

    public List<TopicTestDTO> getTests() { return tests; }
    public void setTests(List<TopicTestDTO> tests) { this.tests = tests; }
}

