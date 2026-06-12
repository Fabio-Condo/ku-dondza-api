package com.fabiocondo.dto;

public class SaveFlashCardRequest {

    private Long flashCardId;

    private boolean saved;

    public Long getFlashCardId() {
        return flashCardId;
    }

    public void setFlashCardId(Long flashCardId) {
        this.flashCardId = flashCardId;
    }

    public boolean isSaved() {
        return saved;
    }

    public void setSaved(boolean saved) {
        this.saved = saved;
    }
}
