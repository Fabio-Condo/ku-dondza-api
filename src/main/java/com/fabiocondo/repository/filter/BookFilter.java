package com.fabiocondo.repository.filter;

import com.fabiocondo.domain.Subject;

public class BookFilter {

    private String searchParam;

    private String exameOrderBy;

    private Subject subject;

    private String name;

    private String description;

    private String urlFile;

    private Long totalDownloadNumber;

    public String getSearchParam() {
        return searchParam;
    }

    public void setSearchParam(String searchParam) {
        this.searchParam = searchParam;
    }

    public String getExameOrderBy() {
        return exameOrderBy;
    }

    public void setExameOrderBy(String exameOrderBy) {
        this.exameOrderBy = exameOrderBy;
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrlFile() {
        return urlFile;
    }

    public void setUrlFile(String urlFile) {
        this.urlFile = urlFile;
    }

    public Long getTotalDownloadNumber() {
        return totalDownloadNumber;
    }

    public void setTotalDownloadNumber(Long totalDownloadNumber) {
        this.totalDownloadNumber = totalDownloadNumber;
    }
}
