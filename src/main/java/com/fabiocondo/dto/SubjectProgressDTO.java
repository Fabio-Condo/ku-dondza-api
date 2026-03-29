package com.fabiocondo.dto;

import com.fabiocondo.enumeration.Category;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.util.List;

public class SubjectProgressDTO {

    private Long id;
    private String subjectId;

    private String subjectName;

    private String subjectDescription;

    @Enumerated(EnumType.STRING)
    private Category subjectCategory;

    private List<TopicDtoWithTests> topicDtoWithTests;

    private double currentUserProgressRate;

    private Long totalTopics;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getSubjectDescription() {
        return subjectDescription;
    }

    public void setSubjectDescription(String subjectDescription) {
        this.subjectDescription = subjectDescription;
    }

    public Category getSubjectCategory() {
        return subjectCategory;
    }

    public void setSubjectCategory(Category subjectCategory) {
        this.subjectCategory = subjectCategory;
    }

    public List<TopicDtoWithTests> getTopicDtoWithTests() {
        return topicDtoWithTests;
    }

    public void setTopicDtoWithTests(List<TopicDtoWithTests> topicDtoWithTests) {
        this.topicDtoWithTests = topicDtoWithTests;
    }

    public double getCurrentUserProgressRate() {
        return currentUserProgressRate;
    }

    public void setCurrentUserProgressRate(double currentUserProgressRate) {
        this.currentUserProgressRate = currentUserProgressRate;
    }

    public Long getTotalTopics() {
        return totalTopics;
    }

    public void setTotalTopics(Long totalTopics) {
        this.totalTopics = totalTopics;
    }
}
