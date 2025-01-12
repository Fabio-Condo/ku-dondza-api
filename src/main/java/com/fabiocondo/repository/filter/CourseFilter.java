package com.fabiocondo.repository.filter;

import com.fabiocondo.domain.Institution;

public class CourseFilter {

    private String searchParam;

    private String courseOrderBy;

    private String name;

    private String level;

    private Institution institution;

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

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
    }
}
