package com.fabiocondo.domain;

import javax.persistence.*;

@Entity
@Table(name = "online_course_content")
public class OnlineCourseContent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String description;

    private String fileName;

    private String urlFile; // URL for storing the location of the video or file in the bucket

    @ManyToOne
    @JoinColumn(name = "online_course_id")
    private OnlineCourse onlineCourse;

    // Constructors
    public OnlineCourseContent() {}

    public OnlineCourseContent(String title, String description, String fileName, String urlFile) {
        this.title = title;
        this.description = description;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public OnlineCourse getOnlineCourse() {
        return onlineCourse;
    }

    public void setOnlineCourse(OnlineCourse onlineCourse) {
        this.onlineCourse = onlineCourse;
    }
}

