package com.fabiocondo.repository.filter;

import com.fabiocondo.domain.Subject;
import com.fabiocondo.enumeration.CompetitionType;
import com.fabiocondo.enumeration.DifficultyLevel;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

public class CompetitionFilter {

    private String searchParam;

    private String sort;

    private Subject subject;

    @Enumerated(EnumType.STRING)
    private DifficultyLevel difficultyLevel;

    @Enumerated(EnumType.STRING)
    private CompetitionType competitionType;

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

    public DifficultyLevel getDifficultyLevel() {
        return difficultyLevel;
    }

    public void setDifficultyLevel(DifficultyLevel difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }

    public CompetitionType getCompetitionType() {
        return competitionType;
    }

    public void setCompetitionType(CompetitionType competitionType) {
        this.competitionType = competitionType;
    }
}
