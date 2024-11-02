package com.fabiocondo.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "competition")
public class Competition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @JsonIgnore
    @ManyToMany
    @JoinTable(
            name = "competition_questions",
            joinColumns = @JoinColumn(name = "competition_id"),
            inverseJoinColumns = @JoinColumn(name = "question_id")
    )
    private Set<Question> questions = new HashSet<>();

    @JsonIgnore
    @ManyToMany
    @JoinTable(
            name = "competition_users",
            joinColumns = @JoinColumn(name = "competition_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> participants = new HashSet<>();

    @JsonIgnore
    @ManyToMany
    @JoinTable(
            name = "participation_request",
            joinColumns = @JoinColumn(name = "to_competition_id"),
            inverseJoinColumns = @JoinColumn(name = "from_user_id")
    )
    private Set<User> participationRequests = new HashSet<>(); // pedidos de participation na competition

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

    public Set<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(Set<Question> questions) {
        this.questions = questions;
    }

    public Set<User> getParticipants() {
        return participants;
    }

    public void setParticipants(Set<User> participants) {
        this.participants = participants;
    }

    public Set<User> getParticipationRequests() {
        return participationRequests;
    }

    public void setParticipationRequests(Set<User> participationRequests) {
        this.participationRequests = participationRequests;
    }
}
