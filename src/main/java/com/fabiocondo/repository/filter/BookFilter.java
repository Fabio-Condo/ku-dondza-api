package com.fabiocondo.repository.filter;

import com.fabiocondo.domain.Subject;

public class BookFilter {

    private String searchParam;

    private String blogOrderBy;

    private Subject subject;

    private String name;

    private String description;

    public String getSearchParam() {
        return searchParam;
    }

    public void setSearchParam(String searchParam) {
        this.searchParam = searchParam;
    }

    public String getBlogOrderBy() {
        return blogOrderBy;
    }

    public void setBlogOrderBy(String blogOrderBy) {
        this.blogOrderBy = blogOrderBy;
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
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
}