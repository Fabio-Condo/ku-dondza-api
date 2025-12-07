package com.fabiocondo.enumeration;

public enum TopicTestStatus {
    LOCKED("LOCKED"),
    ACTIVE("ACTIVE"),
    COMPLETE("COMPLETE");

    private final String label;

    TopicTestStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
