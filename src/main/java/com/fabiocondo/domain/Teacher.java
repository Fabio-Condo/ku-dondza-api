package com.fabiocondo.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "teacher")
public class Teacher implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, updatable=false)
    @JsonProperty(access = JsonProperty.Access.READ_WRITE)
    private Long id;

    private String name;

    private String email;

    private String contactNumber;

    private String fileName;

    private String urlFile;

    @ManyToMany(cascade = CascadeType.DETACH)
    @JoinTable(
            name = "subject_teacher",
            joinColumns = @JoinColumn(name = "subject_id"),
            inverseJoinColumns = @JoinColumn(name = "teacher_id")
    )
    private List<Subject> subjects = new ArrayList<>();

    public Teacher() {
    }

    public Teacher(String name, String email, String contactNumber, String fileName, String urlFile) {
        this.name = name;
        this.email = email;
        this.contactNumber = contactNumber;
        this.fileName = fileName;
        this.urlFile = urlFile;
    }

    public void addSubjectToTeacherSubjectsList(Subject subject) {
        subjects.add(subject);
        subject.getTeachers().add(this);
    }

    public void removeSubjectFromTeacherSubjectsList(Subject subject) {
        subjects.remove(subject);
        subject.getTeachers().remove(this);
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

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public List<Subject> getSubjects() {
        return subjects;
    }

    public void setSubjects(List<Subject> subjects) {
        this.subjects = subjects;
    }
}
