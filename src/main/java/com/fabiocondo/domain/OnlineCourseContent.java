package com.fabiocondo.domain;

import com.fabiocondo.enumeration.ContentType;

import javax.persistence.*;

@Entity
@Table(name = "online_course_content")
public class OnlineCourseContent { // Conteúdo do tema - Depois renomear

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String description;

    @Enumerated(EnumType.STRING)
    private ContentType contentType;

    private String fileName;

    private String urlFile; // URL for storing the location of the video or file in the bucket

    @ManyToOne
    @JoinColumn(name = "tema_id")
    private Tema tema;

    // Constructors
    public OnlineCourseContent() {}

    public OnlineCourseContent(String description, ContentType contentType, String fileName, String urlFile) {
        this.description = description;
        this.contentType = contentType;
        this.fileName = fileName;
        this.urlFile = urlFile;
    }

    // Getters and Setters
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

    public ContentType getContentType() {
        return contentType;
    }

    public void setContentType(ContentType contentType) {
        this.contentType = contentType;
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

    public Tema getTema() {
        return tema;
    }

    public void setTema(Tema tema) {
        this.tema = tema;
    }
}

