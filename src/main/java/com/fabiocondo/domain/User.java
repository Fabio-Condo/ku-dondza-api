package com.fabiocondo.domain;

import com.fabiocondo.enumeration.Plan;
import com.fabiocondo.enumeration.UserType;
import com.fasterxml.jackson.annotation.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;

@Entity
@Table(name = "user")
public class User implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, updatable = false)
    @JsonProperty(access = JsonProperty.Access.READ_WRITE)
    private Long id;

    private String userId;

    private String fullName;

    private String username;

    private String bio;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    private String email;

    private String profileImageUrl;

    private String fileName;

    private String profileCoverImageUrl;

    private String fileNameCoverImage;

    private Date lastLoginDate;

    private Date lastLoginDateDisplay;

    private Date joinDate;

    @Enumerated(EnumType.STRING)
    private UserType userType;

    private String role; //ROLE_USER{ read, edit }, ROLE_ADMIN {delete}

    @Column(length = 1000)
    private String[] authorities;

    private boolean isActive;

    private boolean isNotLocked;

    @Enumerated(EnumType.STRING)
    private Plan plan; //FREE or PREMIUM

    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<ExternalAuthMethod> externalAuthMethods = new HashSet<>();

    @ManyToMany(cascade = CascadeType.DETACH)
    @JoinTable(
            name = "user_subject_interest",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "subject_id")
    )
    private Set<Subject> subjectsInterests = new HashSet<>();

    @JsonIgnore
    @ManyToMany(cascade = CascadeType.DETACH)
    @JoinTable(
            name = "user_online_course",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "course_id")
    )
    private Set<OnlineCourse> subscribedOnlineCourses = new HashSet<>();

    @JsonIgnore
    @ManyToMany(cascade = CascadeType.DETACH)
    @JoinTable(
            name = "user_marked_online_course_content",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "online_course_content_id")
    )
    private Set<OnlineCourseContent> markedCourseContents = new HashSet<>(); // marcado como assistidos

    public User(){}

    public User(Long id, String userId, String fullName, String username, String bio, String password, String email, String profileImageUrl, String fileName, Date lastLoginDate, Date lastLoginDateDisplay, Date joinDate, String role, String[] authorities, boolean isActive, boolean isNotLocked) {
        this.id = id;
        this.userId = userId;
        this.fullName = fullName;
        this.username = username;
        this.bio = bio;
        this.password = password;
        this.email = email;
        this.profileImageUrl = profileImageUrl;
        this.fileName = fileName;
        this.lastLoginDate = lastLoginDate;
        this.lastLoginDateDisplay = lastLoginDateDisplay;
        this.joinDate = joinDate;
        this.role = role;
        this.authorities = authorities;
        this.isActive = isActive;
        this.isNotLocked = isNotLocked;
    }

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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public String getFileName() {
        return fileName;
    }

    public String getProfileCoverImageUrl() {
        return profileCoverImageUrl;
    }

    public void setProfileCoverImageUrl(String profileCoverImageUrl) {
        this.profileCoverImageUrl = profileCoverImageUrl;
    }

    public String getFileNameCoverImage() {
        return fileNameCoverImage;
    }

    public void setFileNameCoverImage(String fileNameCoverImage) {
        this.fileNameCoverImage = fileNameCoverImage;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Date getLastLoginDate() {
        return lastLoginDate;
    }

    public void setLastLoginDate(Date lastLoginDate) {
        this.lastLoginDate = lastLoginDate;
    }

    public Date getLastLoginDateDisplay() {
        return lastLoginDateDisplay;
    }

    public void setLastLoginDateDisplay(Date lastLoginDateDisplay) {
        this.lastLoginDateDisplay = lastLoginDateDisplay;
    }

    public Date getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(Date joinDate) {
        this.joinDate = joinDate;
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

    public String[] getAuthorities() {
        return authorities;
    }

    public void setAuthorities(String[] authorities) {
        this.authorities = authorities;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public boolean isNotLocked() {
        return isNotLocked;
    }

    public void setNotLocked(boolean notLocked) {
        isNotLocked = notLocked;
    }

    public Plan getPlan() {
        return plan;
    }

    public void setPlan(Plan plan) {
        this.plan = plan;
    }

    public Set<ExternalAuthMethod> getExternalAuthMethods() {
        return externalAuthMethods;
    }

    public void setExternalAuthMethods(Set<ExternalAuthMethod> externalAuthMethods) {
        this.externalAuthMethods = externalAuthMethods;
    }

    public Set<Subject> getSubjectsInterests() {
        return subjectsInterests;
    }

    public void setSubjectsInterests(Set<Subject> subjectsInterests) {
        this.subjectsInterests = subjectsInterests;
    }

    public Set<OnlineCourse> getSubscribedOnlineCourses() {
        return subscribedOnlineCourses;
    }

    public void setSubscribedOnlineCourses(Set<OnlineCourse> subscribedOnlineCourses) {
        this.subscribedOnlineCourses = subscribedOnlineCourses;
    }

    public Set<OnlineCourseContent> getMarkedCourseContents() {
        return markedCourseContents;
    }

    public void setMarkedCourseContents(Set<OnlineCourseContent> markedCourseContents) {
        this.markedCourseContents = markedCourseContents;
    }

}
