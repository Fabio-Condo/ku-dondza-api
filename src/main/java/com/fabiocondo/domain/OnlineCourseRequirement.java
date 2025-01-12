package com.fabiocondo.domain;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;

@Entity
@Table(name = "online_course_requirement")
public class OnlineCourseRequirement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty
    private String designation;

    @ManyToOne
    @JoinColumn(name = "online_course_id")
    private OnlineCourse course;

    public OnlineCourseRequirement() {
    }

    public OnlineCourseRequirement(Long id, String designation, OnlineCourse course) {
        this.id = id;
        this.designation = designation;
        this.course = course;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public OnlineCourse getCourse() {
        return course;
    }

    public void setCourse(OnlineCourse course) {
        this.course = course;
    }
}
