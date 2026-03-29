package com.fabiocondo.dto;

import com.fabiocondo.enumeration.Category;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

public class SubjectProgressDTO {

    private Long id;

    private String subjectId;

    private String name;

    private String description;

    @Enumerated(EnumType.STRING)
    private Category category;

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
