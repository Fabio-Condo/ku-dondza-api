package com.fabiocondo.tutor_ai;

public class TutorRequest {

    private Long questionId;

    private Long selectedAnswerId;

    private String message;

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public Long getSelectedAnswerId() {
        return selectedAnswerId;
    }

    public void setSelectedAnswerId(Long selectedAnswerId) {
        this.selectedAnswerId = selectedAnswerId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
