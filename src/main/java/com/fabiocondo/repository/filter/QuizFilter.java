package com.fabiocondo.repository.filter;

public class QuizFilter {

    private String searchParam;

    private String sort;

    private Long subjectId;

    private Long userId;

    public String getSearchParam() {
        return searchParam;
    }

    public void setSearchParam(String searchParam) {
        this.searchParam = searchParam;
    }

    public String getSort() { return sort; }

    public void setSort(String sort) { this.sort = sort; }

    public Long getSubjectId() { return subjectId; }

    public void setSubjectId(Long subjectId) { this.subjectId = subjectId; }

    public Long getUserId() { return userId; }

    public void setUserId(Long userId) { this.userId = userId; }
}