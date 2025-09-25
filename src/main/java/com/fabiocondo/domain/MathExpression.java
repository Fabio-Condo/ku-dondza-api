package com.fabiocondo.domain;

import javax.persistence.*;

@Entity
@Table(name = "math_expression")
public class MathExpression {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, updatable = false)
    private Long id;

    private String name; // Exemplo: "f(x)"

    @Lob
    private String expression; // Exemplo: "x^2 + 2x - 3"

    @ManyToOne
    @JoinColumn(name = "question_id")
    private Question question;

    public MathExpression() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }
}

