package com.fabiocondo.domain;

import javax.persistence.*;

@Entity
@Table(name = "prize")
public class Prize {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int position; // 1 para primeiro lugar, 2 para segundo, etc.
    private String description;

    @ManyToOne
    @JoinColumn(name = "competition_id")
    private Competition competition;

    @OneToOne(mappedBy = "prize", cascade = CascadeType.ALL)
    private PrizeAssignment assignment;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Competition getCompetition() {
        return competition;
    }

    public void setCompetition(Competition competition) {
        this.competition = competition;
    }

    public PrizeAssignment getAssignment() {
        return assignment;
    }

    public void setAssignment(PrizeAssignment assignment) {
        this.assignment = assignment;
    }
}

