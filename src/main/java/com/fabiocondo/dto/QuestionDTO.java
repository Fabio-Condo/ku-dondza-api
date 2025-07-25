package com.fabiocondo.dto;

import com.fabiocondo.domain.Answer;
import com.fabiocondo.domain.MathExpression;
import com.fabiocondo.domain.Topic;
import com.fabiocondo.enumeration.DifficultyLevel;

import javax.persistence.*;

import java.util.List;

public class QuestionDTO {

    private Long id;

    private String questionId;

    @Enumerated(EnumType.STRING)
    private DifficultyLevel difficultyLevel;

    private String text;

    private String tip;

    private String solution;

    private Integer timeLimit; // Tempo em segundos

    private String fileName;

    private String urlFile;

    private Topic topic;

    private List<MathExpression> mathExpressions; // Se for math

    private List<Answer> answers;

    private boolean savedByUser;

    private long numberOfComments;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getQuestionId() {
        return questionId;
    }

    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }

    public DifficultyLevel getDifficultyLevel() {
        return difficultyLevel;
    }

    public void setDifficultyLevel(DifficultyLevel difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTip() {
        return tip;
    }

    public void setTip(String tip) {
        this.tip = tip;
    }

    public String getSolution() {
        return solution;
    }

    public void setSolution(String solution) {
        this.solution = solution;
    }

    public Integer getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(Integer timeLimit) {
        this.timeLimit = timeLimit;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getUrlFile() {
        return urlFile;
    }

    public void setUrlFile(String urlFile) {
        this.urlFile = urlFile;
    }

    public Topic getTopic() {
        return topic;
    }

    public void setTopic(Topic topic) {
        this.topic = topic;
    }

    public List<MathExpression> getMathExpressions() {
        return mathExpressions;
    }

    public void setMathExpressions(List<MathExpression> mathExpressions) {
        this.mathExpressions = mathExpressions;
    }

    public List<Answer> getAnswers() {
        return answers;
    }

    public void setAnswers(List<Answer> answers) {
        this.answers = answers;
    }

    public boolean isSavedByUser() {
        return savedByUser;
    }

    public void setSavedByUser(boolean savedByUser) {
        this.savedByUser = savedByUser;
    }

    public long getNumberOfComments() {
        return numberOfComments;
    }

    public void setNumberOfComments(long numberOfComments) {
        this.numberOfComments = numberOfComments;
    }
}
