package com.fabiocondo.domain;

import com.fabiocondo.enumeration.DifficultyLevel;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "periodic_quiz")
public class PeriodicQuiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String quizId;

    private String title;

    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    private DifficultyLevel difficultyLevel;

    @ManyToOne
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @JsonIgnore
    @ManyToMany
    @JoinTable(
            name = "periodic_quiz_questions",
            joinColumns = @JoinColumn(name = "periodic_quiz_id"),
            inverseJoinColumns = @JoinColumn(name = "question_id")
    )
    private Set<Question> questions = new HashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "periodicQuiz", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<SubmissionTwo> submissions;

    public PeriodicQuiz() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getQuizId() {
        return quizId;
    }

    public void setQuizId(String quizId) {
        this.quizId = quizId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public DifficultyLevel getDifficultyLevel() {
        return difficultyLevel;
    }

    public void setDifficultyLevel(DifficultyLevel difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public Set<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(Set<Question> questions) {
        this.questions = questions;
    }

    public Set<SubmissionTwo> getSubmissions() {
        return submissions;
    }

    public void setSubmissions(Set<SubmissionTwo> submissions) {
        this.submissions = submissions;
    }
}
