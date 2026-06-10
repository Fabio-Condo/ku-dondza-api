package com.fabiocondo.tutor_ai;

public class TutorRequest {

    private Long topicId; // Se for conversa sobre topico - para topicos dentro do curso

    private Long questionId; // Se for conversa sobre question - para exercicios e quizzes

    private Long subjectId; // Se for conversa sobre question - para exercicios e quizzes

    private Long selectedAnswerId; // Se for conversa sobre question - para exercicios e quizzes

    private Long userId;

    private String message;

    public Long getTopicId() {
        return topicId;
    }

    public void setTopicId(Long topicId) {
        this.topicId = topicId;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public Long getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
    }

    public Long getSelectedAnswerId() {
        return selectedAnswerId;
    }

    public void setSelectedAnswerId(Long selectedAnswerId) {
        this.selectedAnswerId = selectedAnswerId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
