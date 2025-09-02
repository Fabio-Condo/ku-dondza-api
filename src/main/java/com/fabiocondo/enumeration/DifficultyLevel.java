package com.fabiocondo.enumeration;

public enum DifficultyLevel {
    EASY("Fácil"),
    MEDIUM("Médio"),
    HARD("Difícil");
//    VERY_HARD("Muito Difícil"),
//    EXPERT("Especialista");

    private final String description;

    DifficultyLevel(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}


