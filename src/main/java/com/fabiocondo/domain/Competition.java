package com.fabiocondo.domain;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "competition")
public class Competition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @ManyToMany
    @JoinTable(
            name = "competition_questions",
            joinColumns = @JoinColumn(name = "competition_id"),
            inverseJoinColumns = @JoinColumn(name = "question_id")
    )
    private List<Question> questions;

    @ManyToMany
    @JoinTable(
            name = "competition_users",
            joinColumns = @JoinColumn(name = "competition_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> participants;

    public Competition() {
    }

    public Competition(String title) {
        this.title = title;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }

    public List<User> getParticipants() {
        return participants;
    }

    public void setParticipants(List<User> participants) {
        this.participants = participants;
    }
}
