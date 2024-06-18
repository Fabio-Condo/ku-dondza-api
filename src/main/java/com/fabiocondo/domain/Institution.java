package com.fabiocondo.domain;

import com.fabiocondo.enumeration.AdministrationType;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;

@Entity(name = "instituicao")
public class Institution { // ADD SIGLA - UEM, UP, ACIPOL

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, updatable=false)
    @JsonProperty(access = JsonProperty.Access.READ_WRITE)
    private Long id;

    private String name;

    private String type;  // ensino superior, ensino técnico

    @Enumerated(EnumType.STRING)
    private AdministrationType administrationType;

    private String address; // Rua, Número, Bairro, Cidade, Estado

    private String website;

    private String description;

    private String fileName;

    private String urlFile;

    public Institution() {
    }

    public Institution(Long id, String name, String type, AdministrationType administrationType, String address, String website, String description, String fileName, String urlFile) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.administrationType = administrationType;
        this.address = address;
        this.website = website;
        this.description = description;
        this.fileName = fileName;
        this.urlFile = urlFile;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public AdministrationType getAdministrationType() {
        return administrationType;
    }

    public void setAdministrationType(AdministrationType administrationType) {
        this.administrationType = administrationType;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getUrlFile() {
        return urlFile;
    }

    public void setUrlFile(String urlFile) {
        this.urlFile = urlFile;
    }
}
