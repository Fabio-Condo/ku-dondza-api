package com.fabiocondo.dto;

import com.fabiocondo.domain.Answer;
import com.fabiocondo.domain.User;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

public class AnswerWithUserDTO {
    private Answer answer;

    @JsonIgnoreProperties(value={"hibernateLazyInitializer"})
    private User user;

    public AnswerWithUserDTO(Answer answer, User user) {
        this.answer = answer;
        this.user = user;
    }

    public Answer getAnswer() {
        return answer;
    }

    public void setAnswer(Answer answer) {
        this.answer = answer;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
