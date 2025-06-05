package com.fabiocondo.enumeration;

public enum CompetitionType {

    KNOWLEDGE_CUP("Knowledge Cup"),             // Copa do Conhecimento (Competição Geral)
    SCHOOL_LEAGUE("School Knowledge League"),   // Liga Escolar do Saber (Competição Interna)
    GENIUS_TOURNAMENT("Genius Tournament");     // Torneio dos Gênios (Melhores de cada escola)

    private final String description;
    CompetitionType(String description) {
        this.description = description;
    }

    public boolean requiresAuthorization() {
        return this == SCHOOL_LEAGUE || this == GENIUS_TOURNAMENT;
    }

    public String getDescription() {
        return description;
    }
}



