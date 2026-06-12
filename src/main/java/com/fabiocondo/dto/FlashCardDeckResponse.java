package com.fabiocondo.dto;

import java.util.List;

public class FlashCardDeckResponse {

    private Long topicId;

    private String topicName;

    private String subjectId;

    private String subjectName;

    private List<FlashCardResponse> cards;

    public Long getTopicId() {
        return topicId;
    }

    public void setTopicId(Long topicId) {
        this.topicId = topicId;
    }

    public String getTopicName() {
        return topicName;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public List<FlashCardResponse> getCards() {
        return cards;
    }

    public void setCards(List<FlashCardResponse> cards) {
        this.cards = cards;
    }
}
