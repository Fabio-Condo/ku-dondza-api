package com.fabiocondo.domain;

import com.fabiocondo.enumeration.NotificationType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties(value={"hibernateLazyInitializer", "bio", "email", "fileName", "profileCoverImageUrl", "fileNameCoverImage", "lastLoginDateDisplay", "joinDate", "userType", "role", "authorities", "lastLoginDate", "active", "notLocked"})
    private User user; // Quem recebe a notificação

    @ManyToOne
    @JoinColumn(name = "sender_id")
    @JsonIgnoreProperties(value={"hibernateLazyInitializer", "bio", "email", "fileName", "profileCoverImageUrl", "fileNameCoverImage", "lastLoginDateDisplay", "joinDate", "userType", "role", "authorities", "lastLoginDate", "active", "notLocked"})
    private User sender; // Quem realizou a ação

    @JsonIgnoreProperties({"subject", "difficultyLevel", "status", "prizes", "startedAt", "endedAt", "winners"})
    @ManyToOne
    @JoinColumn(name = "competition_id")
    private Competition competition;

    private String message;

    @Enumerated(EnumType.STRING)
    private NotificationType type;

    @Column(name = "reference_id")
    private Long referenceId; // ID do post, comentário ou grupo relacionado à notificação

    @Column(name = "is_read")
    private boolean isRead;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // Getters e Setters


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

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public Competition getCompetition() {
        return competition;
    }

    public void setCompetition(Competition competition) {
        this.competition = competition;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public Long getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(Long referenceId) {
        this.referenceId = referenceId;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
