package com.fabiocondo.repository.filter;

import com.fabiocondo.domain.User;

public class OnlineCourseFilter {

    private String searchParam;

    private String courseOrderBy;

    private String name;

    private User instrutor;

    private User user;

    public String getSearchParam() {
        return searchParam;
    }

    public void setSearchParam(String searchParam) {
        this.searchParam = searchParam;
    }

    public String getCourseOrderBy() {
        return courseOrderBy;
    }

    public void setCourseOrderBy(String courseOrderBy) {
        this.courseOrderBy = courseOrderBy;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public User getInstrutor() { return instrutor; }

    public void setInstrutor(User instrutor) { this.instrutor = instrutor; }

    public User getUser() { return user; }

    public void setUser(User user) { this.user = user; }
}