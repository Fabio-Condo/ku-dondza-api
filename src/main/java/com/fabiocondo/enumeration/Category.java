package com.fabiocondo.enumeration;

public enum Category {
    EXACT_SCIENCES("Exact Sciences"),
    HUMAN_SCIENCES("Human Sciences"),
    LANGUAGES("Languages");

    private final String label;

    Category(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
