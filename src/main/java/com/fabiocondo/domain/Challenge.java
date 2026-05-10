package com.fabiocondo.domain;

import com.fabiocondo.enumeration.DifficultyLevel;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity @Table(name = "challenges")
public class Challenge {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, updatable=false)
    @JsonProperty(access = JsonProperty.Access.READ_WRITE)
    private Long id; @Column(unique = true, nullable = false)

    private String challengeId;

    private String title; @Column(length = 500)

    private String description; @Enumerated(EnumType.STRING)

    private DifficultyLevel difficultyLevel;

    private Integer xpReward;

    private Date startDate;

    private Date endDate;

    @ManyToOne
    @JoinColumn(name = "subject_id")
    private Subject subject;

    @JsonIgnore
    @ManyToMany(cascade = CascadeType.DETACH)
    @JoinTable( name = "challenge_questions", joinColumns = @JoinColumn(name = "challenge_id"), inverseJoinColumns = @JoinColumn(name = "question_id") )
    @OrderBy("id ASC")
    private Set<Question> challengeQuestions = new HashSet<>();

    @JsonIgnore
    @ManyToMany(cascade = CascadeType.DETACH)
    @JoinTable(name = "submitted_challenge_quizzes", joinColumns = @JoinColumn(name = "challenge_id"), inverseJoinColumns = @JoinColumn(name = "quiz_id") )
    private Set<Quiz> submittedChallengeQuizzes = new HashSet<>();

    public Challenge() { }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getChallengeId() { return challengeId; }
    public void setChallengeId(String challengeId) { this.challengeId = challengeId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public DifficultyLevel getDifficultyLevel() { return difficultyLevel; }
    public void setDifficultyLevel(DifficultyLevel difficultyLevel) { this.difficultyLevel = difficultyLevel; }
    public Integer getXpReward() { return xpReward; }
    public void setXpReward(Integer xpReward) { this.xpReward = xpReward; }
    public Date getStartDate() { return startDate; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }
    public Date getEndDate() { return endDate; }
    public void setEndDate(Date endDate) { this.endDate = endDate; }
    public Subject getSubject() { return subject; }
    public void setSubject(Subject subject) { this.subject = subject; }
    public Set<Question> getChallengeQuestions() { return challengeQuestions; }
    public void setChallengeQuestions(Set<Question> challengeQuestions) { this.challengeQuestions = challengeQuestions; }
    public Set<Quiz> getSubmittedChallengeQuizzes() { return submittedChallengeQuizzes; }
    public void setSubmittedChallengeQuizzes(Set<Quiz> submittedChallengeQuizzes) { this.submittedChallengeQuizzes = submittedChallengeQuizzes; }
}