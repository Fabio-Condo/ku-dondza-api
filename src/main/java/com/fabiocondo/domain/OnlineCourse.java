package com.fabiocondo.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "online_course")
public class OnlineCourse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String onlineCourseId;

    private String name;

    private String description;

    private String fileName;

    private String coverImageUrl; // URL da imagem de capa do curso

    private String requirements;

    private String lunchDate;

    private String instrutorName;

    private String instrutorDescription;

    private String instrutorSpecialization;

    @JsonIgnoreProperties({"onlineCourse"})
    @OneToMany(mappedBy = "onlineCourse", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Tema> temas = new ArrayList<>();

    @JsonIgnore
    @ManyToMany
    @JoinTable(
            name = "online_course_questions",
            joinColumns = @JoinColumn(name = "online_course_id"),
            inverseJoinColumns = @JoinColumn(name = "question_id")
    )
    private Set<Question> questions = new HashSet<>();

    @JsonIgnore
    @ManyToMany(mappedBy = "subscribedOnlineCourses")
    private List<User> students;

    // Constructors
    public OnlineCourse() {}

    public OnlineCourse(String name, String description, String coverImageUrl, String requirements, String lunchDate, String instrutorName, String instrutorDescription, String instrutorSpecialization) {
        this.name = name;
        this.description = description;
        this.coverImageUrl = coverImageUrl;
        this.requirements = requirements;
        this.lunchDate = lunchDate;
        this.instrutorName = instrutorName;
        this.instrutorDescription = instrutorDescription;
        this.instrutorSpecialization = instrutorSpecialization;
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

    public String getRequirements() {
        return requirements;
    }

    public void setRequirements(String requirements) {
        this.requirements = requirements;
    }

    public String getLunchDate() {
        return lunchDate;
    }

    public void setLunchDate(String lunchDate) {
        this.lunchDate = lunchDate;
    }

    public String getInstrutorName() {
        return instrutorName;
    }

    public void setInstrutorName(String instrutorName) {
        this.instrutorName = instrutorName;
    }

    public String getInstrutorDescription() {
        return instrutorDescription;
    }

    public void setInstrutorDescription(String instrutorDescription) {
        this.instrutorDescription = instrutorDescription;
    }

    public String getInstrutorSpecialization() {
        return instrutorSpecialization;
    }

    public void setInstrutorSpecialization(String instrutorSpecialization) {
        this.instrutorSpecialization = instrutorSpecialization;
    }

    public List<Tema> getTemas() {
        return temas;
    }

    public void setTemas(List<Tema> temas) {
        this.temas = temas;
    }

    public Set<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(Set<Question> questions) {
        this.questions = questions;
    }

    public List<User> getStudents() {
        return students;
    }

    public void setStudents(List<User> students) {
        this.students = students;
    }
}

