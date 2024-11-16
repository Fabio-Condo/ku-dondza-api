package com.fabiocondo.service;

import com.fabiocondo.domain.Post;
import com.fabiocondo.exception.domain.*;
import com.fabiocondo.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.MessagingException;
import java.io.IOException;
import java.util.List;
import java.util.Set;

public interface UserService {

    User register(String firstName, String lastName, String username, String email) throws UserNotFoundException, MessagingException, UsernameExistException, EmailExistException;

    User addNewUser(String firstName, String lastName, String username, String email, String role, boolean isNonLocked, boolean isActive, MultipartFile profileImage) throws UserNotFoundException, UsernameExistException, EmailExistException, MessagingException;

    User updateUser(String currentUsername, String newFirstName, String newLastName, String newUsername, String newEmail, String role, boolean isNonLocked, boolean isActive, MultipartFile profileImage) throws UserNotFoundException, UsernameExistException, EmailExistException;

    User updateUserProfile(String currentUsername, String newFirstName, String newLastName, String newUsername, String newEmail, String newBio, String role, boolean isNonLocked, boolean isActive, MultipartFile profileImage) throws UserNotFoundException, UsernameExistException, EmailExistException;

    User update(User user, Long id) throws CourseNotFoundException;

    User updateUserProfilePhoto(String currentUsername, MultipartFile profileImage) throws IOException;

    User updateUserProfileCoverPhoto(String currentUsername, MultipartFile profileImage) throws IOException;

    void resetPassword(String email) throws MessagingException, EmailNotFoundException;

    void deleteUser(String username) throws IOException;

    User updateProfileImage(String username, MultipartFile profileImage) throws UsernameExistException, EmailExistException, IOException, UserNotFoundException;

    void updatePropertyActive(String username, Boolean active) throws UsernameNotFoundException;

    void updatePropertyNotLocked(String username, Boolean notLocked) throws UsernameNotFoundException;

    boolean checkFriendship(Long friendId) throws UserNotFoundException;

    boolean checkIfSentFriendRequest(Long receptorUserId, Long emissorUserId) throws UserNotFoundException;

    User addCourseToSubscribedOnlineCourses(Long userId, Long onlineCourseId) throws CourseNotFoundException;

    User removeCourseFromSubscribedOnlineCourses(Long userId, Long onlineCourseId) throws CourseNotFoundException;

    boolean doesUserSubscribedOnlineCourse(Long userId, Long onlineCourseId);

    long countFriendsByUserId(Long userId);

    long countFriendRequestsByUserId(Long userId);

    User findUserByUsername(String username);

    User findUserByUserId(String userId);

    User findUserByEmail(String email);

    List<User> getUsers();

    Page<User> findAll(String searchParam, Pageable pageable) throws UserNotFoundException;

    long getTotal();

    User addInterestToUserInterests(Long userId, Long postId) throws InterestNotFoundException;

    User removeInterestFromUserInterests(Long userId, Long postId) throws InterestNotFoundException;

    User addPostToSavedPosts(Long userId, Long postId) throws PostNotFoundException;

    User removePostFromSavedPosts(Long userId, Long postId) throws PostNotFoundException;

    List<Post> getSavedPosts(Long id) throws UserNotFoundException;

    Page<Post> findSavedPostsByUserId(Long userId, Pageable pageable) throws UserNotFoundException;

    long countSavedPostsByUser(Long userId) throws UserNotFoundException;

    boolean checkIfUserSavedPost(Long userId, Long postId);

    Set<User> getFriendRequests() throws UserNotFoundException;

    Page<User> getCurrentFriendRequests(Pageable pageable) throws UserNotFoundException;

    Page<User> getFriends(Long userId, Pageable pageable) throws UserNotFoundException;

    Page<User> getCurrentUserFriends(Pageable pageable) throws UserNotFoundException;

    void sendFriendRequest(User friend) throws UserNotFoundException;

    User acceptFriendRequest(Long friendId) throws UserNotFoundException;

    void rejectFriendRequest(Long friendId) throws UserNotFoundException;

    Set<User> getFriends() throws UserNotFoundException;

    void removeFriend(Long friendId) throws UserNotFoundException;
}
