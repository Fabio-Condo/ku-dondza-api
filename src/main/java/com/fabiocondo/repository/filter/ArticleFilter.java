package com.fabiocondo.repository.filter;

import com.fabiocondo.enumeration.CategoryType;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

public class ArticleFilter {

    private String searchParam;

    @Enumerated(EnumType.STRING)
    private CategoryType category;

    private String sort;

    private String title;

    private Long userId;

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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public CategoryType getCategory() {
        return category;
    }

    public void setCategory(CategoryType category) {
        this.category = category;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
