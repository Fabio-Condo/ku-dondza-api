package com.fabiocondo.dto;

import com.fabiocondo.enumeration.FlashCardStatus;

public class FlashCardProgressRequest {

    private Long flashCardId;

    private FlashCardStatus status;

    public Long getFlashCardId() {
        return flashCardId;
    }

    public void setFlashCardId(Long flashCardId) {
        this.flashCardId = flashCardId;
    }

    public FlashCardStatus getStatus() {
        return status;
    }

    public void setStatus(FlashCardStatus status) {
        this.status = status;
    }
}
