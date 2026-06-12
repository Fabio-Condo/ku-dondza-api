package com.fabiocondo.domain;

import com.fabiocondo.enumeration.FlashCardStatus;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_flash_card_progress")
public class UserFlashCardProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "flash_card_id")
    private FlashCard flashCard;

    @Enumerated(EnumType.STRING)
    private FlashCardStatus status;

    private boolean saved;

    private Integer reviewCount;

    private LocalDateTime lastReviewedAt;

    private LocalDateTime nextReviewDate;

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

    public FlashCard getFlashCard() {
        return flashCard;
    }

    public void setFlashCard(FlashCard flashCard) {
        this.flashCard = flashCard;
    }

    public FlashCardStatus getStatus() {
        return status;
    }

    public void setStatus(FlashCardStatus status) {
        this.status = status;
    }

    public boolean isSaved() {
        return saved;
    }

    public void setSaved(boolean saved) {
        this.saved = saved;
    }

    public Integer getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(Integer reviewCount) {
        this.reviewCount = reviewCount;
    }

    public LocalDateTime getLastReviewedAt() {
        return lastReviewedAt;
    }

    public void setLastReviewedAt(LocalDateTime lastReviewedAt) {
        this.lastReviewedAt = lastReviewedAt;
    }

    public LocalDateTime getNextReviewDate() {
        return nextReviewDate;
    }

    public void setNextReviewDate(LocalDateTime nextReviewDate) {
        this.nextReviewDate = nextReviewDate;
    }
}
