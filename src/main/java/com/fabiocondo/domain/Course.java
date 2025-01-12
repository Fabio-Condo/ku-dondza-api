package com.fabiocondo.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import javax.validation.Valid;
import java.io.Serializable;
import java.util.List;

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

    private String level;  // Técnico, Licenciatura, Mestrado, Doutorado

    @ManyToOne
    @JoinColumn(name = "institution_id")
    private Institution institution;

    @Valid
    @JsonIgnoreProperties("course")
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CourseRequirement> requirements;

    public Course() {
    }

    public Course(Long id, String name, String duration, Institution institution) {
        this.id = id;
        this.name = name;
        this.duration = duration;
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

    public void setDuration(String duration) { this.duration = duration; }

    public String getLevel() { return level; }

    public void setLevel(String level) { this.level = level; }

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
    }

    public List<CourseRequirement> getRequirements() {
        return requirements;
    }

    public void setRequirements(List<CourseRequirement> requirements) {
        this.requirements = requirements;
    }
}
