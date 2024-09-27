package com.fabiocondo.domain;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "online_course")
public class OnlineCourse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String description;

    private String fileName;

    private String coverImageUrl; // URL da imagem de capa do curso

    @OneToMany(mappedBy = "onlineCourse", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OnlineCourseContent> courseContents = new ArrayList<>();

    // Constructors
    public OnlineCourse() {}

    public OnlineCourse(String name, String description, String coverImageUrl) {
        this.name = name;
        this.description = description;
        this.coverImageUrl = coverImageUrl;
    }

    // Getters and Setters
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

    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    public void setCoverImageUrl(String coverImageUrl) {
        this.coverImageUrl = coverImageUrl;
    }

    public List<OnlineCourseContent> getCourseContents() {
        return courseContents;
    }

    public void setCourseContents(List<OnlineCourseContent> courseContents) {
        this.courseContents = courseContents;
    }

    // Method to add a Course Content
    public void addCourseContent(OnlineCourseContent content) {
        courseContents.add(content);
        content.setOnlineCourse(this);
    }

    // Method to remove a Course Content
    public void removeCourseContent(OnlineCourseContent content) {
        courseContents.remove(content);
        content.setOnlineCourse(null);
    }
}

