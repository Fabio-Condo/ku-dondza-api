package com.fabiocondo.repository.filter;

import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

public class ExameFilter {

    private String global;

    private String vocationOrderBy;

    private String institution;

    private String description;

    private String level;

    private String subject;

    private String urlFile;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date beginDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date endDate;

    private Long totalDownloadNumber;

    public String getGlobal() {
        return global;
    }

    public void setGlobal(String global) {
        this.global = global;
    }

    public String getVocationOrderBy() {
        return vocationOrderBy;
    }

    public void setVocationOrderBy(String vocationOrderBy) {
        this.vocationOrderBy = vocationOrderBy;
    }

    public String getInstitution() {
        return institution;
    }

    public void setInstitution(String institution) {
        this.institution = institution;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getUrlFile() {
        return urlFile;
    }

    public void setUrlFile(String urlFile) {
        this.urlFile = urlFile;
    }

    public Date getBeginDate() {
        return beginDate;
    }

    public void setBeginDate(Date beginDate) {
        this.beginDate = beginDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Long getTotalDownloadNumber() {
        return totalDownloadNumber;
    }

    public void setTotalDownloadNumber(Long totalDownloadNumber) {
        this.totalDownloadNumber = totalDownloadNumber;
    }
}
