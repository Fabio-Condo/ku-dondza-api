package com.fabiocondo.repository.filter;

import com.fabiocondo.domain.Subject;
import com.fabiocondo.domain.Topic;
import com.fabiocondo.enumeration.DifficultyLevel;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

public class QuestionFilter {

    private String searchParam;

    private String sort;

    private Subject subject;

    private Topic topic;

    private String text;

    @Enumerated(EnumType.STRING)
    private DifficultyLevel difficultyLevel;

    private Long userId;

    private Boolean  highlighted;

    public String getSearchParam() {
        return searchParam;
    }

    public void setSearchParam(String searchParam) {
        this.searchParam = searchParam;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public Topic getTopic() {
        return topic;
    }

    public void setTopic(Topic topic) {
        this.topic = topic;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public DifficultyLevel getDifficultyLevel() {
        return difficultyLevel;
    }

    public void setDifficultyLevel(DifficultyLevel difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Boolean isHighlighted() {
        return highlighted;
    }

    public void setHighlighted(Boolean highlighted) {
        this.highlighted = highlighted;
    }
}