package com.fabiocondo.dto;

import com.fabiocondo.enumeration.Plan;
import com.fabiocondo.enumeration.UserType;

import javax.persistence.*;

public class UserDTO {
    private Long id;

    private String userId;

    private String fullName;

    private String email;

    private String bio;

    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    private UserType userType;

    @Enumerated(EnumType.STRING)
    private Plan plan; //FREE or PREMIUM

    private double markedContentRate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public UserType getUserType() {
        return userType;
    }

    public void setUserType(UserType userType) {
        this.userType = userType;
    }

    public Plan getPlan() {
        return plan;
    }

    public void setPlan(Plan plan) {
        this.plan = plan;
    }

    public double getMarkedContentRate() {
        return markedContentRate;
    }

    public void setMarkedContentRate(double markedContentRate) {
        this.markedContentRate = markedContentRate;
    }

}
