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

    private String email;

    private String bio;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

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
            name = "user_subject_subscription",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "subject_id")
    )
    private Set<Subject> subscribedSubjects = new HashSet<>();

    @JsonIgnore
    @ManyToMany(cascade = CascadeType.DETACH)
    @JoinTable(
            name = "user_marked_topic_content",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "topic_content_id")
    )
    private Set<TopicContent> markedTopicContents = new HashSet<>(); // marcado como assistidos

    @JsonIgnore
    @ManyToMany(cascade = CascadeType.DETACH)
    @JoinTable(
            name = "user_course",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "course_id")
    )
    private Set<Course> subscribedCourses = new HashSet<>();

    @JsonIgnore
    @ManyToMany(cascade = CascadeType.DETACH)
    @JoinTable(
            name = "user_marked_content",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "content_id")
    )
    private Set<Content> markedContents = new HashSet<>(); // marcado como assistidos

    @JsonIgnore
    @ManyToMany(cascade = CascadeType.DETACH)
    @JoinTable(
            name = "user_saved_article",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "article_id")
    )
    private Set<Article> savedArticles = new HashSet<>();

    @JsonIgnore
    @ManyToMany(cascade = CascadeType.DETACH)
    @JoinTable(
            name = "user_saved_question",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "question_id")
    )
    private Set<Question> savedQuestions = new HashSet<>();

    @JsonIgnoreProperties({"user"})
    @OneToMany(mappedBy = "user")
    private List<PrizeAssignment> prizeAssignments;

    public User(){}

    public User(Long id, String userId, String fullName, String email, String bio, String password, String profileImageUrl, String fileName, Date lastLoginDate, Date lastLoginDateDisplay, Date joinDate, String role, String[] authorities, boolean isActive, boolean isNotLocked) {
        this.id = id;
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.bio = bio;
        this.password = password;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public Set<Subject> getSubscribedSubjects() {
        return subscribedSubjects;
    }

    public void setSubscribedSubjects(Set<Subject> subscribedSubjects) {
        this.subscribedSubjects = subscribedSubjects;
    }

    public Set<TopicContent> getMarkedTopicContents() {
        return markedTopicContents;
    }

    public void setMarkedTopicContents(Set<TopicContent> markedTopicContents) {
        this.markedTopicContents = markedTopicContents;
    }

    public Set<Course> getSubscribedCourses() {
        return subscribedCourses;
    }

    public void setSubscribedCourses(Set<Course> subscribedCourses) {
        this.subscribedCourses = subscribedCourses;
    }

    public Set<Content> getMarkedContents() {
        return markedContents;
    }

    public void setMarkedContents(Set<Content> markedContents) {
        this.markedContents = markedContents;
    }

    public Set<Article> getSavedArticles() {
        return savedArticles;
    }

    public void setSavedArticles(Set<Article> savedArticles) {
        this.savedArticles = savedArticles;
    }

    public Set<Question> getSavedQuestions() {
        return savedQuestions;
    }

    public void setSavedQuestions(Set<Question> savedQuestions) {
        this.savedQuestions = savedQuestions;
    }

    public List<PrizeAssignment> getPrizeAssignments() {
        return prizeAssignments;
    }

    public void setPrizeAssignments(List<PrizeAssignment> prizeAssignments) {
        this.prizeAssignments = prizeAssignments;
    }
}
