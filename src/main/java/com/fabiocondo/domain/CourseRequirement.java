package com.fabiocondo.domain;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;

@Entity
@Table(name = "course_requirement")
public class CourseRequirement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty
    private String designation;

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;

    public CourseRequirement() {
    }

    public CourseRequirement(Long id, String designation, Course course) {
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

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }
}
