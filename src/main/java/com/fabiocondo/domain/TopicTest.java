package com.fabiocondo.domain;

import com.fabiocondo.enumeration.DifficultyLevel;
import com.fabiocondo.enumeration.TopicTestStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "topic_test")
public class TopicTest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, updatable = false)
    @JsonProperty(access = JsonProperty.Access.READ_WRITE)
    private Long id;

    @Enumerated(EnumType.STRING)
    private DifficultyLevel difficultyLevel;

    @Enumerated(EnumType.STRING)
    private TopicTestStatus topicTestStatus;

    //@JsonIgnoreProperties("questions")
    //@JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id", nullable = false)
    @JsonIgnoreProperties(
            value = { "questions", "contents", "hibernateLazyInitializer", "handler" }
    )
    private Topic topic;

    @JsonIgnore
    @ManyToMany(cascade = CascadeType.DETACH)
    @JoinTable(
            name = "topic_test_questions",
            joinColumns = @JoinColumn(name = "topic_test_id"),
            inverseJoinColumns = @JoinColumn(name = "question_id")
    )
    @OrderBy("id ASC")
    private Set<Question> questions = new HashSet<>();

    @JsonIgnore
    @ManyToMany(cascade = CascadeType.DETACH)
    @JoinTable(
            name = "submitted_quizzes",
            joinColumns = @JoinColumn(name = "topic_test_id"),
            inverseJoinColumns = @JoinColumn(name = "quiz_id")
    )
    private Set<Quiz> submittedQuizzes = new HashSet<>();

    private int orderIndex; // 1, 2, 3

    public TopicTest() {
    }

    public TopicTest(Long id, DifficultyLevel difficultyLevel, Topic topic, int orderIndex) {
        this.id = id;
        this.difficultyLevel = difficultyLevel;
        this.topic = topic;
        this.orderIndex = orderIndex;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public DifficultyLevel getDifficultyLevel() {
        return difficultyLevel;
    }

    public void setDifficultyLevel(DifficultyLevel difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }

    public TopicTestStatus getTopicTestStatus() {
        return topicTestStatus;
    }

    public void setTopicTestStatus(TopicTestStatus topicTestStatus) {
        this.topicTestStatus = topicTestStatus;
    }

    public Topic getTopic() {
        return topic;
    }

    public void setTopic(Topic topic) {
        this.topic = topic;
    }

    public Set<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(Set<Question> questions) {
        this.questions = questions;
    }

    public Set<Quiz> getSubmittedQuizzes() {
        return submittedQuizzes;
    }

    public void setSubmittedQuizzes(Set<Quiz> submittedQuizzes) {
        this.submittedQuizzes = submittedQuizzes;
    }

    public int getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
    }
}
