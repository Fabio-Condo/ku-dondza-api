package com.fabiocondo.repository.filter;

import java.util.Date;

public class ExameFilter {

    private String global;

    private String vocationOrderBy;

    private String description;

    private String level;

    private String subject;

    private String urlFile;

    private Date date;

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

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Long getTotalDownloadNumber() {
        return totalDownloadNumber;
    }

    public void setTotalDownloadNumber(Long totalDownloadNumber) {
        this.totalDownloadNumber = totalDownloadNumber;
    }
}
