package com.fabiocondo.dto;

import com.fabiocondo.domain.Question;
import com.fabiocondo.domain.TopicContent;


import java.util.List;

public class TopicDTO {

    private Long id;

    private String topicId;

    private String name;

    private String description;

    private SubjectDto subject;

    private boolean enabled;

    private boolean premium = false;

    private Integer position;

    private List<Question> questions;

    private List<TopicContentDTO> contents;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTopicId() {
        return topicId;
    }

    public void setTopicId(String topicId) {
        this.topicId = topicId;
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

    public SubjectDto getSubject() {
        return subject;
    }

    public void setSubject(SubjectDto subject) {
        this.subject = subject;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isPremium() {
        return premium;
    }

    public void setPremium(boolean premium) {
        this.premium = premium;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }

    public List<TopicContentDTO> getContents() {
        return contents;
    }

    public void setContents(List<TopicContentDTO> contents) {
        this.contents = contents;
    }
}
