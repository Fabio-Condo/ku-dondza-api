package com.fabiocondo.dto;

import com.fabiocondo.domain.Topic;
import com.fabiocondo.enumeration.ContentType;

import javax.persistence.*;

public class TopicContentDTO {

    private Long id;

    private String description;

    @Enumerated(EnumType.STRING)
    private ContentType contentType;

    private String time;

    private String fileName;

    private String urlFile; // URL for storing the location of the video or file in the bucket

    private Integer position; // Novo campo para controlar a posição do conteúdo

    @Transient // Nao salvar na bd
    private boolean markedByUser;


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

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
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

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public boolean isMarkedByUser() {
        return markedByUser;
    }

    public void setMarkedByUser(boolean markedByUser) {
        this.markedByUser = markedByUser;
    }
}
