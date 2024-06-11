package com.fabiocondo.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import java.util.Date;

@Entity(name = "InstituicaoController")
public class Instituicao {

    //Nome da Instituição
    //Tipo de Instituição (e.g., escola, faculdade, universidade)
    //Nível de Ensino (e.g., fundamental, médio, superior, pós-graduação)
    //Endereço (Rua, Número, Bairro, Cidade, Estado, CEP)
    //Telefone de Contato
    //E-mail de Contato
    //Website
    //Ano de Fundação

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, updatable=false)
    @JsonProperty(access = JsonProperty.Access.READ_WRITE)
    private Long id;

    private String type;  // escola, faculdade, universidade

    private String level; // fundamental, médio, superior, pós-graduação

    private String address; // Rua, Número, Bairro, Cidade, Estado

    private String telephone;

    private String email;

    private String website;

    private Date foundationDate;

    public Instituicao() {
    }

    public Instituicao(Long id, String type, String level, String address, String telephone, String email, String website, Date foundationDate) {
        this.id = id;
        this.type = type;
        this.level = level;
        this.address = address;
        this.telephone = telephone;
        this.email = email;
        this.website = website;
        this.foundationDate = foundationDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public Date getFoundationDate() {
        return foundationDate;
    }

    public void setFoundationDate(Date foundationDate) {
        this.foundationDate = foundationDate;
    }
}
