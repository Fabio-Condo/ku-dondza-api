package com.fabiocondo.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import java.util.Date;

@Entity(name = "exame")
public class Exame {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, updatable=false)
    @JsonProperty(access = JsonProperty.Access.READ_WRITE)
    private Long id;

    private String description; // 10 classe, 12 classe, ACIPOL, UP, UEM, ISRI

    private String level; // Ensino Geral, Admissao ao Ensino Tecnico, Admissao ao Ensino Superior

    private String subject;

    private String urlFile;

    private Date date;

    private Long totalDownloadNumber;

    public Exame() {
    }

    public Exame(String description, String level, String subject) {
        this.description = description;
        this.level = level;
        this.subject = subject;
    }

    public Exame(Long id, String description, String level, String subject, String urlFile, Date date, Long totalDownloadNumber) {
        this.id = id;
        this.description = description;
        this.level = level;
        this.subject = subject;
        this.urlFile = urlFile;
        this.date = date;
        this.totalDownloadNumber = totalDownloadNumber;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
