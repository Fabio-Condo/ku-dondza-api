package com.fabiocondo.repository.filter;

import com.fabiocondo.domain.Subject;
import com.fabiocondo.domain.User;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

public class QuizFilter {

    private String searchParam;

    private String sort;

    private Subject subject;

    private User user;

    public String getSearchParam() {
        return searchParam;
    }

    public void setSearchParam(String searchParam) {
        this.searchParam = searchParam;
    }

    public String getSort() { return sort; }

    public void setSort(String sort) { this.sort = sort; }

    public Subject getSubject() { return subject; }

    public void setSubject(Subject subject) { this.subject = subject; }

    public User getUser() { return user; }

    public void setUser(User user) { this.user = user; }
}