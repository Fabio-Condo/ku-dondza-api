package com.fabiocondo.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.persistence.*;

@Entity
@Table(name = "topic")
public class Topic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, updatable = false)
    @JsonProperty(access = JsonProperty.Access.READ_WRITE)
    private Long id;

    private String name;

    @ManyToOne
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    private boolean isReadyForQuiz;

    public Topic() {
    }

    public Topic(Long id, String name, Subject subject) {
        this.id = id;
        this.name = name;
        this.subject = subject;
    }

    // Getters e Setters
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

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public boolean isReadyForQuiz() {
        return isReadyForQuiz;
    }

    public void setReadyForQuiz(boolean readyForQuiz) {
        isReadyForQuiz = readyForQuiz;
    }
}