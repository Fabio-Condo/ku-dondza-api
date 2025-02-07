package com.fabiocondo.domain;

import com.fabiocondo.enumeration.RankingPosition;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;

@Entity
@Table(name = "prize")
public class Prize {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String description;

    @Enumerated(EnumType.STRING)
    private RankingPosition position;  // Ex: FIRST_PLACE, SECOND_PLACE, etc.

    @JsonIgnoreProperties({"winners"})
    @ManyToOne
    @JoinColumn(name = "competition_id", nullable = false)
    private Competition competition;

    public Prize() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public RankingPosition getPosition() {
        return position;
    }

    public void setPosition(RankingPosition position) {
        this.position = position;
    }

    public Competition getCompetition() {
        return competition;
    }

    public void setCompetition(Competition competition) {
        this.competition = competition;
    }
}

