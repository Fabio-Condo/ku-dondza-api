package com.fabiocondo.dto;

import javax.persistence.*;

public class MathExpressionDTO {

    private Long id;

    private String name; // Exemplo: "f(x)"

    @Lob
    private String expression; // Exemplo: "x^2 + 2x - 3"

    private QuestionDTO question;

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

    public QuestionDTO getQuestion() {
        return question;
    }

    public void setQuestion(QuestionDTO question) {
        this.question = question;
    }
}
