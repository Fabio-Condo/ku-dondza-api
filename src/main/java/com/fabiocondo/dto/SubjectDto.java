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
}
