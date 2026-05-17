package com.fabiocondo.repository.filter;

import com.fabiocondo.domain.Subject;
import com.fabiocondo.enumeration.ExamType;
import com.fabiocondo.enumeration.Institution;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

public class ExamFilter {

    private String searchParam;

    private String sort;

    private Subject subject;

    @Enumerated(EnumType.STRING)
    private ExamType examType;

    @Enumerated(EnumType.STRING)
    private Institution institution;

    private String level;

    private Long beginYear;

    private Long endYear;

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

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public ExamType getExamType() {
        return examType;
    }

    public void setExamType(ExamType examType) {
        this.examType = examType;
    }

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public Long getBeginYear() {
        return beginYear;
    }

    public void setBeginYear(Long beginYear) {
        this.beginYear = beginYear;
    }

    public Long getEndYear() {
        return endYear;
    }

    public void setEndYear(Long endYear) {
        this.endYear = endYear;
    }
}