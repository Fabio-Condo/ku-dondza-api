package com.fabiocondo.dto;

import com.fabiocondo.domain.Module;
import com.fabiocondo.domain.User;

import java.util.ArrayList;
import java.util.List;

public class OnlineCourseDTO {

    private Long id;

    private String onlineCourseId;

    private String name;

    private String description;

    private String fileName;

    private String coverImageUrl; // URL da imagem de capa do curso

    private String lunchDate;

    private User instrutor;

    private List<Module> modules = new ArrayList<>(); // Mesmo com JsonIgnore na class model, se nao colocar aqui, sera serealizado

    private boolean isCurrentUserSubscribed;

    // Constructors
    public OnlineCourseDTO() {}

    public OnlineCourseDTO(String name, String description, String coverImageUrl, String lunchDate) {
        this.name = name;
        this.description = description;
        this.coverImageUrl = coverImageUrl;
        this.lunchDate = lunchDate;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOnlineCourseId() {
        return onlineCourseId;
    }

    public void setOnlineCourseId(String onlineCourseId) {
        this.onlineCourseId = onlineCourseId;
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

    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    public void setCoverImageUrl(String coverImageUrl) {
        this.coverImageUrl = coverImageUrl;
    }

    public String getLunchDate() {
        return lunchDate;
    }

    public void setLunchDate(String lunchDate) {
        this.lunchDate = lunchDate;
    }

    public List<Module> getModules() {
        return modules;
    }

    public void setModules(List<Module> modules) {
        this.modules = modules;
    }

    public User getInstrutor() {
        return instrutor;
    }

    public void setInstrutor(User instrutor) {
        this.instrutor = instrutor;
    }

    public boolean isCurrentUserSubscribed() {
        return isCurrentUserSubscribed;
    }

    public void setCurrentUserSubscribed(boolean currentUserSubscribed) {
        isCurrentUserSubscribed = currentUserSubscribed;
    }

}

