package com.fabiocondo.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tema")
public class Tema {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, updatable = false)
    @JsonProperty(access = JsonProperty.Access.READ_WRITE)
    private Long id;

    private String name;

    @ManyToOne
    @JoinColumn(name = "online_course_id")
    private OnlineCourse onlineCourse;

    @JsonIgnoreProperties({"tema"})
    @OneToMany(mappedBy = "tema", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OnlineCourseContent> courseContents = new ArrayList<>();

    public Tema() {
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

    public OnlineCourse getOnlineCourse() {
        return onlineCourse;
    }

    public void setOnlineCourse(OnlineCourse onlineCourse) {
        this.onlineCourse = onlineCourse;
    }

    public List<OnlineCourseContent> getCourseContents() {
        return courseContents;
    }

    public void setCourseContents(List<OnlineCourseContent> courseContents) {
        this.courseContents = courseContents;
    }
}
