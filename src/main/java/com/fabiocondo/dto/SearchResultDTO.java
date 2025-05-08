package com.fabiocondo.dto;

public class SearchResultDTO {
    private String type;    // Tipo do resultado (e.g., "User", "Article")
    private String content; // Conteúdo do resultado (e.g., nome do usuário, conteúdo do post)
    private String urlFile;
    private String id;

    public SearchResultDTO(String type, String content, String urlFile, String id) {
        this.type = type;
        this.content = content;
        this.urlFile = urlFile;
        this.id = id;
    }

    // Getters e Setters
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUrlFile() {
        return urlFile;
    }

    public void setUrlFile(String urlFile) {
        this.urlFile = urlFile;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}

