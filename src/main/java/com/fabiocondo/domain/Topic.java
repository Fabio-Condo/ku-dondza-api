package com.fabiocondo.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "topic")
public class Topic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, updatable = false)
    @JsonProperty(access = JsonProperty.Access.READ_WRITE)
    private Long id;

    private String topicId;

    private String name;

    private String description;

    @ManyToOne
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    private boolean isReadyForQuiz;

    private Integer position; // Novo campo para controlar a posição do conteúdo

    @JsonIgnoreProperties("topic")
    @OneToMany(mappedBy = "topic", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Question> questions;

    @JsonIgnoreProperties("topic")
    @OneToMany(mappedBy = "topic", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC") // Ordena os conteúdos ao carregar
    private List<TopicContent> contents;

    public Topic() {
    }

    public Topic(String topicId, String name, String description, Subject subject) {
        this.topicId = topicId;
        this.name = name;
        this.description = description;
        this.subject = subject;
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTopicId() {
        return topicId;
    }

    public void setTopicId(String topicId) {
        this.topicId = topicId;
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

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }

    public List<TopicContent> getContents() {
        return contents;
    }

    public void setContents(List<TopicContent> contents) {
        this.contents = contents;
    }
}