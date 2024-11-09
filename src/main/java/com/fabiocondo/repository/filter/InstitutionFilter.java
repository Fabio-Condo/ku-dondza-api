package com.fabiocondo.repository.filter;

import com.fabiocondo.enumeration.AdministrationType;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

public class InstitutionFilter {

    private String searchParam;

    private String institutionOrderBy;

    private String name;

    private String type;

    @Enumerated(EnumType.STRING)
    private AdministrationType administrationType;

    public InstitutionFilter(String searchParam, String institutionOrderBy, String name, String type, AdministrationType administrationType) {
        this.searchParam = searchParam;
        this.institutionOrderBy = institutionOrderBy;
        this.name = name;
        this.type = type;
        this.administrationType = administrationType;
    }

    public String getSearchParam() {
        return searchParam;
    }

    public void setSearchParam(String searchParam) {
        this.searchParam = searchParam;
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
}
