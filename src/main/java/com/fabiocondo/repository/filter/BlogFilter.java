package com.fabiocondo.repository.filter;

import com.fabiocondo.domain.Subject;

public class BlogFilter {

    private String searchParam;

    private String blogOrderBy;

    private Subject subject;

    private String title;

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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
