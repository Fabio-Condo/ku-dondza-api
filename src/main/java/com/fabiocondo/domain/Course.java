package com.fabiocondo.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "course")
public class Course implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, updatable=false)
    @JsonProperty(access = JsonProperty.Access.READ_WRITE)
    private Long id;

    private String name;

    private String duration;

    private String requirements;

    @ManyToOne
    @JoinColumn(name = "institution_id")
    private Institution institution;

    public Course() {
    }

    public Course(Long id, String name, String duration, String requirements, Institution institution) {
        this.id = id;
        this.name = name;
        this.duration = duration;
        this.requirements = requirements;
        this.institution = institution;
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

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getRequirements() {
        return requirements;
    }

    public void setRequirements(String requirements) {
        this.requirements = requirements;
    }

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
    }
}
