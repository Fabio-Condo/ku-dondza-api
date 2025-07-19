package com.fabiocondo.domain;

import javax.persistence.*;
import java.time.LocalDateTime;


@Entity
@Table(name = "comments")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //private Long exerciseId; // ou topicId, dependendo do uso

    @Column(columnDefinition = "TEXT")
    private String content;

    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "question_id")
    private Question question;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    //@ManyToOne(fetch = FetchType.LAZY)
    //@JoinColumn(name = "parent_id")
    //private Comment parent;

    //@OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    //private List<Comment> replies;

    // ===== Getters and Setters =====

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    //public Comment getParent() {
    //    return parent;
    //}

    //public void setParent(Comment parent) {
    //    this.parent = parent;
    //}

    //public List<Comment> getReplies() {
    //    return replies;
    //}

    //public void setReplies(List<Comment> replies) {
    //    this.replies = replies;
    //}

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
