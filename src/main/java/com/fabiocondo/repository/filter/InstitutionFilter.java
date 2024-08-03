package com.fabiocondo.repository.filter;

import com.fabiocondo.enumeration.AdministrationType;
import com.fabiocondo.enumeration.Country;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

public class InstitutionFilter {

    private String global;

    private String institutionOrderBy;

    private String name;

    private String type;

    @Enumerated(EnumType.STRING)
    private AdministrationType administrationType;

    @Enumerated(EnumType.STRING)
    private Country country;

    public InstitutionFilter(String global, String institutionOrderBy, String name, String type, AdministrationType administrationType, Country country) {
        this.global = global;
        this.institutionOrderBy = institutionOrderBy;
        this.name = name;
        this.type = type;
        this.administrationType = administrationType;
        this.country = country;
    }

    public String getGlobal() {
        return global;
    }

    public void setGlobal(String global) {
        this.global = global;
    }

    public String getInstitutionOrderBy() {
        return institutionOrderBy;
    }

    public void setInstitutionOrderBy(String institutionOrderBy) {
        this.institutionOrderBy = institutionOrderBy;
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

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }
}
