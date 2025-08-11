package com.fabiocondo.dto;

import com.fabiocondo.domain.Topic;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

public class SubjectDto {

    private Long id;

    private String subjectId;

    private String name;

    private String description;

    private boolean isCurrentUserSubscribed;

    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private List<Topic> topics;

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

    public boolean isCurrentUserSubscribed() {
        return isCurrentUserSubscribed;
    }

    public void setCurrentUserSubscribed(boolean currentUserSubscribed) {
        isCurrentUserSubscribed = currentUserSubscribed;
    }

    public List<Topic> getTopics() {
        return topics;
    }

    public void setTopics(List<Topic> topics) {
        this.topics = topics;
    }
}
