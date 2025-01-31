package com.fabiocondo.domain;

import com.fabiocondo.enumeration.RankingPosition;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;

@Entity
@Table(name = "competition_winner")
public class CompetitionWinner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "competition_id")
    private Competition competition;

    @Enumerated(EnumType.STRING)
    private RankingPosition position;  // Ex: FIRST_PLACE, SECOND_PLACE, etc.

    @ManyToOne
    @JoinColumn(name = "prize_id")
    private Prize prize;  // Prêmio atribuído ao vencedor

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties(value={"hibernateLazyInitializer"})
    private User user;

    public CompetitionWinner() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Competition getCompetition() {
        return competition;
    }

    public void setCompetition(Competition competition) {
        this.competition = competition;
    }

    public RankingPosition getPosition() {
        return position;
    }

    public void setPosition(RankingPosition position) {
        this.position = position;
    }

    public Prize getPrize() {
        return prize;
    }

    public void setPrize(Prize prize) {
        this.prize = prize;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}

