package com.fabiocondo.dto;

import com.fabiocondo.enumeration.DifficultyLevel;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.util.List;

public class QuestionDTO {

    private Long id;

    private String questionId;

    private String text;

    private String tip;

    private String solution;

    @Enumerated(EnumType.STRING)
    private DifficultyLevel difficultyLevel;

    private Integer timeLimit; // Tempo em segundos

    private boolean validated;

    private String fileName;

    private String urlFile;

    private boolean highlighted;

    @JsonIgnoreProperties(
            value = { "questions", "hibernateLazyInitializer", "handler" }
    ) // ADICIONANDO PORCAUSA DE TOPIC_TEST (MODULO DE PROGRESSAO) - MELHORAR DEPOIS COM DTO
    private TopicDTO topic;

    private List<MathExpressionDTO> mathExpressions; // Se for math

    private List<AnswerDTO> answers;

    private boolean savedByUser;

    private long numberOfComments;

    private boolean selected;

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

    public DifficultyLevel getDifficultyLevel() {
        return difficultyLevel;
    }

    public void setDifficultyLevel(DifficultyLevel difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }

    public Integer getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(Integer timeLimit) {
        this.timeLimit = timeLimit;
    }

    public boolean isValidated() {
        return validated;
    }

    public void setValidated(boolean validated) {
        this.validated = validated;
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

    public boolean isHighlighted() {
        return highlighted;
    }

    public void setHighlighted(boolean highlighted) {
        this.highlighted = highlighted;
    }

    public TopicDTO getTopic() {
        return topic;
    }

    public void setTopic(TopicDTO topic) {
        this.topic = topic;
    }

    public List<MathExpressionDTO> getMathExpressions() {
        return mathExpressions;
    }

    public void setMathExpressions(List<MathExpressionDTO> mathExpressions) {
        this.mathExpressions = mathExpressions;
    }

    public List<AnswerDTO> getAnswers() {
        return answers;
    }

    public void setAnswers(List<AnswerDTO> answers) {
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

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
