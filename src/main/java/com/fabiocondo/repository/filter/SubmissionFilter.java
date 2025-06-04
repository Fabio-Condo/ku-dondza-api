package com.fabiocondo.repository.filter;


public class SubmissionFilter {

    private String searchParam;

    private String submissionOrderBy;

    private String title;

    private Long userId;

    private Long CompetitionId;

    public String getSearchParam() {
        return searchParam;
    }

    public void setSearchParam(String searchParam) {
        this.searchParam = searchParam;
    }

    public String getSubmissionOrderBy() {
        return submissionOrderBy;
    }

    public void setSubmissionOrderBy(String submissionOrderBy) {
        this.submissionOrderBy = submissionOrderBy;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getCompetitionId() {
        return CompetitionId;
    }

    public void setCompetitionId(Long competitionId) {
        CompetitionId = competitionId;
    }
}
