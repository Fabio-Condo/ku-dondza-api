package com.fabiocondo.domain;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(
        name = "user_subject_score",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "subject_id"})
        }
)
public class UserSubjectScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @Column(nullable = false)
    private Long score = 0L;

    private Integer passedTests = 0;

    private Integer attempts = 0;

    private Date updatedAt;

    public UserSubjectScore() {
    }

    public UserSubjectScore(User user, Subject subject, Long score, Integer passedTests, Integer attempts, Date updatedAt) {
        this.user = user;
        this.subject = subject;
        this.score = score;
        this.passedTests = passedTests;
        this.attempts = attempts;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public Long getScore() {
        return score;
    }

    public void setScore(Long score) {
        this.score = score;
    }

    public Integer getPassedTests() {
        return passedTests;
    }

    public void setPassedTests(Integer passedTests) {
        this.passedTests = passedTests;
    }

    public Integer getAttempts() {
        return attempts;
    }

    public void setAttempts(Integer attempts) {
        this.attempts = attempts;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
}
