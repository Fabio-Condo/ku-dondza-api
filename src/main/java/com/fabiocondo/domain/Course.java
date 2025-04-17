package com.fabiocondo.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "course")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String onlineCourseId;

    private String name;

    private String description;

    private String fileName;

    private String coverImageUrl; // URL da imagem de capa do curso

    private String lunchDate;

    @ManyToOne
    @JsonIgnoreProperties({"subscribedCourses"})
    @JoinColumn(name = "user_id")
    private User instrutor;

    //@JsonIgnoreProperties({"onlineCourse"})
    @JsonIgnore
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Module> modules = new ArrayList<>();

    @JsonIgnore
    @ManyToMany(mappedBy = "subscribedCourses")
    private List<User> students;

    // Constructors
    public Course() {}

    public Course(String name, String description, String coverImageUrl, String lunchDate) {
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

    public List<User> getStudents() {
        return students;
    }

    public void setStudents(List<User> students) {
        this.students = students;
    }
}

