package com.fabiocondo.repository.filter;

import com.fabiocondo.enumeration.UserType;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

public class UserFilter {

    private String searchParam;

    private String userOrderBy;

    private String fullName;

    private String email;

    @Enumerated(EnumType.STRING)
    private UserType userType;

    private String role;

    public String getSearchParam() {
        return searchParam;
    }

    public void setSearchParam(String searchParam) {
        this.searchParam = searchParam;
    }

    public String getUserOrderBy() {
        return userOrderBy;
    }

    public void setUserOrderBy(String userOrderBy) {
        this.userOrderBy = userOrderBy;
    }


    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public UserType getUserType() {
        return userType;
    }

    public void setUserType(UserType userType) {
        this.userType = userType;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}