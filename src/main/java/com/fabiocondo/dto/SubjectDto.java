package com.fabiocondo.dto;

import com.fabiocondo.domain.Topic;
import com.fabiocondo.enumeration.Category;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.util.List;

public class SubjectDto {

    private Long id;

    private String subjectId;

    private String name;

    private String description;

    @Enumerated(EnumType.STRING)
    private Category category;

    private String fileName;

    private String urlFile;

    // CONTROLO DE MÓDULOS
    private boolean quizEnabled;
    private boolean courseEnabled;
    private boolean progressEnabled;
    private boolean examEnabled;

    private double currentUserMarkedContentRate;

    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private List<Topic> topics;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getUrlFile() {
        return urlFile;
    }

    public void setUrlFile(String urlFile) {
        this.urlFile = urlFile;
    }

    public double getCurrentUserMarkedContentRate() {
        return currentUserMarkedContentRate;
    }

    public void setCurrentUserMarkedContentRate(double currentUserMarkedContentRate) {
        this.currentUserMarkedContentRate = currentUserMarkedContentRate;
    }

    public List<Topic> getTopics() {
        return topics;
    }

    public void setTopics(List<Topic> topics) {
        this.topics = topics;
    }

    public Long getTotalTopics() {
        return totalTopics;
    }

    public void setTotalTopics(Long totalTopics) {
        this.totalTopics = totalTopics;
    }


    public boolean isQuizEnabled() {
        return quizEnabled;
    }

    public void setQuizEnabled(boolean quizEnabled) {
        this.quizEnabled = quizEnabled;
    }

    public boolean isCourseEnabled() {
        return courseEnabled;
    }

    public void setCourseEnabled(boolean courseEnabled) {
        this.courseEnabled = courseEnabled;
    }

    public boolean isProgressEnabled() {
        return progressEnabled;
    }

    public void setProgressEnabled(boolean progressEnabled) {
        this.progressEnabled = progressEnabled;
    }

    public boolean isExamEnabled() {
        return examEnabled;
    }

    public void setExamEnabled(boolean examEnabled) {
        this.examEnabled = examEnabled;
    }
}
