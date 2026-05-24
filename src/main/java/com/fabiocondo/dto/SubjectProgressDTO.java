package com.fabiocondo.dto;

import com.fabiocondo.enumeration.Category;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.util.List;

public class SubjectProgressDTO {

    private Long id;
    private String subjectId;

    private String subjectName;

    private String subjectUrlFile;

    private String subjectDescription;

    @Enumerated(EnumType.STRING)
    private Category subjectCategory;

    // CONTROLO DE MÓDULOS
    private boolean progressEnabled;

    private List<TopicDtoWithTests> topicDtoWithTests;

    private double currentUserTotalTestsScore;

    private double currentUserProgressRate;

    private Long currentUserScore = 0L;

    private Long currentUserRank = 0L;

    private Long totalTopics;

    private Long totalTests;

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

    public String getSubjectUrlFile() {
        return subjectUrlFile;
    }

    public void setSubjectUrlFile(String subjectUrlFile) {
        this.subjectUrlFile = subjectUrlFile;
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

    public double getCurrentUserTotalTestsScore() {
        return currentUserTotalTestsScore;
    }

    public void setCurrentUserTotalTestsScore(double currentUserTotalTestsScore) {
        this.currentUserTotalTestsScore = currentUserTotalTestsScore;
    }

    public double getCurrentUserProgressRate() {
        return currentUserProgressRate;
    }

    public void setCurrentUserProgressRate(double currentUserProgressRate) {
        this.currentUserProgressRate = currentUserProgressRate;
    }

    public Long getCurrentUserScore() {
        return currentUserScore;
    }

    public void setCurrentUserScore(Long currentUserScore) {
        this.currentUserScore = currentUserScore;
    }

    public Long getCurrentUserRank() {
        return currentUserRank;
    }

    public void setCurrentUserRank(Long currentUserRank) {
        this.currentUserRank = currentUserRank;
    }

    public Long getTotalTopics() {
        return totalTopics;
    }

    public void setTotalTopics(Long totalTopics) {
        this.totalTopics = totalTopics;
    }

    public Long getTotalTests() {
        return totalTests;
    }

    public void setTotalTests(Long totalTests) {
        this.totalTests = totalTests;
    }

    public boolean isProgressEnabled() {
        return progressEnabled;
    }

    public void setProgressEnabled(boolean progressEnabled) {
        this.progressEnabled = progressEnabled;
    }
}
