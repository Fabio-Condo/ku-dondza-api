package com.fabiocondo.repository.filter;

import com.fabiocondo.domain.Subject;
import com.fabiocondo.domain.Topic;

public class QuestionFilter {

    private String searchParam;

    private String questionOrderBy;

    private Subject subject;

    private Topic topic;

    private String text;

    public String getSearchParam() {
        return searchParam;
    }

    public void setSearchParam(String searchParam) {
        this.searchParam = searchParam;
    }

    public String getQuestionOrderBy() {
        return questionOrderBy;
    }

    public void setQuestionOrderBy(String questionOrderBy) {
        this.questionOrderBy = questionOrderBy;
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
}