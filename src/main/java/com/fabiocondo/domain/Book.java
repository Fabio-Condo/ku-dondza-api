package com.fabiocondo.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;

@Entity
@Table(name = "book")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, updatable=false)
    @JsonProperty(access = JsonProperty.Access.READ_WRITE)
    private Long id;

    private String name;

    private String description;

    private String fileName;

    private String urlFile;

    private Long totalDownloadNumber;

    @ManyToOne
    @JoinColumn(name = "subject_id")
    private Subject subject;

    public Book() {
    }

    public Book(Long id, String name, String description, String fileName, String urlFile, Long totalDownloadNumber, Subject subject) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.fileName = fileName;
        this.urlFile = urlFile;
        this.totalDownloadNumber = totalDownloadNumber;
        this.subject = subject;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
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

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }
}
