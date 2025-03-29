package com.fabiocondo.service;

import com.fabiocondo.domain.*;
import com.fabiocondo.enumeration.UserType;
import com.fabiocondo.exception.domain.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.MessagingException;
import java.io.IOException;
import java.util.List;
import java.util.Set;

public interface UserService {

    List<User> getAllInstrutores();

    User register(String firstName, String lastName, String username, String email, String profileImageUrl) throws UserNotFoundException, MessagingException, UsernameExistException, EmailExistException;

    User updateUserProfile(String currentUsername, String newFirstName, String newLastName, String newUsername, String newEmail, String newBio, String role, boolean isNonLocked, boolean isActive, MultipartFile profileImage) throws UserNotFoundException, UsernameExistException, EmailExistException;

    User addNewUser(String firstName, String lastName, String username, String email, String role, UserType userType, boolean isNonLocked, boolean isActive, MultipartFile profileImage) throws UserNotFoundException, UsernameExistException, EmailExistException, MessagingException;

    User updateUser(String currentUsername, String newFirstName, String newLastName, String newUsername, String newEmail, String role, UserType userType, boolean isNonLocked, boolean isActive, MultipartFile profileImage) throws UserNotFoundException, UsernameExistException, EmailExistException;

    User update(User user, Long id) throws UserNotFoundException;

    User updateUserProfilePhoto(String currentUsername, MultipartFile profileImage) throws IOException;

    User updateUserProfileCoverPhoto(String currentUsername, MultipartFile profileImage) throws IOException;

    void resetPassword(String email) throws MessagingException, EmailNotFoundException;

    void deleteUser(String username) throws IOException;

    User updateProfileImage(String username, MultipartFile profileImage) throws UsernameExistException, EmailExistException, IOException, UserNotFoundException;

    void updatePropertyActive(String username, Boolean active) throws UsernameNotFoundException;

    void updatePropertyNotLocked(String username, Boolean notLocked) throws UsernameNotFoundException;

    User addContentToMarkedCourseContents(Long userId, Long onlineCourseContentId) throws UserNotFoundException, CourseContentNotFoundException;

    User removeContentFromMarkedCourseContents(Long userId, Long onlineCourseContentId) throws UserNotFoundException, CourseContentNotFoundException;

    boolean checkIfMarkedCourseContent(Long userId, Long onlineCourseContentId);

    Page<OnlineCourse> getSubscribedOnlineCoursesByUserId(Long userId, Pageable pageable) throws UserNotFoundException;

    long countSubscribedOnlineCoursesByUserId(Long userId);

    User addCourseToSubscribedOnlineCourses(Long userId, Long onlineCourseId) throws OnlineCourseNotFoundException, UserNotFoundException;

    User removeCourseFromSubscribedOnlineCourses(Long userId, Long onlineCourseId) throws OnlineCourseNotFoundException, UserNotFoundException;

    User findUserByUsername(String username);

    User findUserByUserId(String userId);

    User findUserByEmail(String email);

    List<User> getUsers();

    Page<User> findAll(String searchParam, Pageable pageable) throws UserNotFoundException;

    long getTotal();

    Set<Subject> getUserSubjectInterests(Long userId) throws UserNotFoundException;

    User addInterestToUserInterests(Long userId, Long postId) throws SubjectNotFoundException, UserNotFoundException;

    User removeInterestFromUserInterests(Long userId, Long postId) throws SubjectNotFoundException, UserNotFoundException;

}
