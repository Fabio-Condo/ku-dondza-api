package com.fabiocondo.domain;

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

    private String firstName;

    private String lastName;

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

    private String role; //ROLE_USER{ read, edit }, ROLE_ADMIN {delete}

    @Column(length = 1000)
    private String[] authorities;

    private boolean isActive;

    private boolean isNotLocked;

    @JsonIgnore
    @ManyToMany
    @JoinTable(
            name = "saved_posts",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "post_id")
    )
    private List<Post> savedPosts;

    @JsonIgnore
    @ManyToMany
    @JoinTable(
            name = "friendship",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "friend_id")
    )
    private Set<User> friends = new HashSet<>();

    @JsonIgnore
    @ManyToMany
    @JoinTable(
            name = "friend_request",
            joinColumns = @JoinColumn(name = "to_user_id"),
            inverseJoinColumns = @JoinColumn(name = "from_user_id")
    )
    private Set<User> friendRequests = new HashSet<>();

    @ManyToMany(cascade = CascadeType.DETACH)
    @JoinTable(
            name = "user_interest",
            joinColumns = @JoinColumn(name = "interest_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<Interest> interests = new HashSet<>();

    @JsonIgnoreProperties({"courseContents"})
    @ManyToMany(cascade = CascadeType.DETACH)
    @JoinTable(
            name = "user_course",
            joinColumns = @JoinColumn(name = "course_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<OnlineCourse> subscribedOnlineCourses = new HashSet<>();

    @JsonIgnore
    @ManyToMany(mappedBy = "participants")
    private List<Competition> competitions;

    public User(){}

    public User(Long id, String userId, String firstName, String lastName, String username, String bio, String password, String email, String profileImageUrl, String fileName, Date lastLoginDate, Date lastLoginDateDisplay, Date joinDate, String role, String[] authorities, boolean isActive, boolean isNotLocked) {
        this.id = id;
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
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

    public boolean isFriend(User user) {
        return user != null && this.friends.contains(user);
    }

    public boolean checkIfSentFriendRequest(User user) {
        return user != null && this.friendRequests.contains(user);
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

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
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

    public List<Post> getSavedPosts() {
        return savedPosts;
    }

    public void setSavedPosts(List<Post> savedPosts) {
        this.savedPosts = savedPosts;
    }

    public Set<User> getFriends() {
        return friends;
    }

    public void setFriends(Set<User> friends) {
        this.friends = friends;
    }

    public Set<User> getFriendRequests() {
        return friendRequests;
    }

    public void setFriendRequests(Set<User> friendRequests) {
        this.friendRequests = friendRequests;
    }

    public void setInterests(Set<Interest> interests) {
        this.interests = interests;
    }

    public Set<Interest> getInterests() {
        return interests;
    }

    public Set<OnlineCourse> getSubscribedOnlineCourses() {
        return subscribedOnlineCourses;
    }

    public void setSubscribedOnlineCourses(Set<OnlineCourse> subscribedOnlineCourses) {
        this.subscribedOnlineCourses = subscribedOnlineCourses;
    }

    public List<Competition> getCompetitions() {
        return competitions;
    }

    public void setCompetitions(List<Competition> competitions) {
        this.competitions = competitions;
    }
}

