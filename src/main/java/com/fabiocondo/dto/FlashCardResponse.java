package com.fabiocondo.dto;

import com.fabiocondo.enumeration.FlashCardStatus;

public class FlashCardResponse {

    private Long id;

    private String question;

    private String answer;

    private String category;

    private String note;

    private boolean saved;

    private FlashCardStatus status;

    private TopicDTO topic;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public boolean isSaved() {
        return saved;
    }

    public void setSaved(boolean saved) {
        this.saved = saved;
    }

    public FlashCardStatus getStatus() {
        return status;
    }

    public void setStatus(FlashCardStatus status) {
        this.status = status;
    }

    public TopicDTO getTopic() {
        return topic;
    }

    public void setTopic(TopicDTO topic) {
        this.topic = topic;
    }
}
