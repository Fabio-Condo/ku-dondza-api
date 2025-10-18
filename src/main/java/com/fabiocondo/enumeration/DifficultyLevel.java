package com.fabiocondo.enumeration;

public enum DifficultyLevel {
    BEGINNER("Beginner"),
    ADVANCED("Advanced");

    private final String label;

    DifficultyLevel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
