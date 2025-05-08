package com.fabiocondo.repository.filter;

import com.fabiocondo.domain.Subject;

public class ArticleFilter {

    private String searchParam;

    private String articleOrderBy;

    private Subject subject;

    private String title;

    public String getSearchParam() {
        return searchParam;
    }

    public void setSearchParam(String searchParam) {
        this.searchParam = searchParam;
    }

    public String getArticleOrderBy() {
        return articleOrderBy;
    }

    public void setArticleOrderBy(String articleOrderBy) {
        this.articleOrderBy = articleOrderBy;
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
