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

    private Long testsCompleted = 0L;

    private Date updatedAt;

    public UserSubjectScore() {
    }

    public UserSubjectScore(User user, Subject subject, Long score, Long testsCompleted, Date updatedAt) {
        this.user = user;
        this.subject = subject;
        this.score = score;
        this.testsCompleted = testsCompleted;
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

    public Long getTestsCompleted() {
        return testsCompleted;
    }

    public void setTestsCompleted(Long testsCompleted) {
        this.testsCompleted = testsCompleted;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }
}
