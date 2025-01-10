package com.fabiocondo.repository.filter;

import com.fabiocondo.domain.Subject;

public class QuizFilter {

    private String searchParam;

    private String quizOrderBy;

    private String title;

    private Subject subject;

    public String getSearchParam() {
        return searchParam;
    }

    public void setSearchParam(String searchParam) {
        this.searchParam = searchParam;
    }

    public String getQuizOrderBy() { return quizOrderBy; }

    public void setQuizOrderBy(String quizOrderBy) { this.quizOrderBy = quizOrderBy; }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Subject getSubject() { return subject; }

    public void setSubject(Subject subject) { this.subject = subject; }
}
