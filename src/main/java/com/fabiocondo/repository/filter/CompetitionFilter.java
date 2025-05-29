package com.fabiocondo.repository.filter;

import com.fabiocondo.domain.Subject;
import com.fabiocondo.enumeration.DifficultyLevel;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

public class CompetitionFilter {

    private String searchParam;

    private String competitionOrderBy;

    private String title;

    private Subject subject;

    @Enumerated(EnumType.STRING)
    private DifficultyLevel difficultyLevel;

    public String getSearchParam() {
        return searchParam;
    }

    public void setSearchParam(String searchParam) {
        this.searchParam = searchParam;
    }

    public String getCompetitionOrderBy() { return competitionOrderBy; }

    public void setCompetitionOrderBy(String competitionOrderBy) { this.competitionOrderBy = competitionOrderBy; }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Subject getSubject() { return subject; }

    public void setSubject(Subject subject) { this.subject = subject; }

    public DifficultyLevel getDifficultyLevel() {
        return difficultyLevel;
    }

    public void setDifficultyLevel(DifficultyLevel difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }
}
