package com.fabiocondo.service;

import com.fabiocondo.domain.*;
import com.fabiocondo.enumeration.UserType;
import com.fabiocondo.exception.domain.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.MessagingException;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface UserService {

    List<User> getAllInstrutores();

    User register(String fullName, String email, String profileImageUrl) throws UserNotFoundException, MessagingException, EmailExistException;

    User updateUserProfile(String currentEmail, String fullName, String newEmail, String newBio, String role, boolean isNonLocked, boolean isActive, MultipartFile profileImage) throws UserNotFoundException, EmailExistException;

    User addNewUser(String fullName, String email, String role, UserType userType, boolean isNonLocked, boolean isActive, MultipartFile profileImage) throws UserNotFoundException, EmailExistException, MessagingException;

    User updateUser(String currentEmail, String fullName, String newEmail, String role, UserType userType, boolean isNonLocked, boolean isActive, MultipartFile profileImage) throws UserNotFoundException, EmailExistException;

    User update(User user, Long id) throws UserNotFoundException;

    User updateUserProfilePhoto(String currentEmail, MultipartFile profileImage) throws IOException, EmailNotFoundException;

    User updateUserProfileCoverPhoto(String currentEmail, MultipartFile profileImage) throws IOException, EmailNotFoundException;

    void resetPassword(String email) throws MessagingException, EmailNotFoundException;

    void deleteUser(String email) throws IOException;

    User updateProfileImage(String email, MultipartFile profileImage) throws EmailExistException, IOException, UserNotFoundException;

    void updatePropertyActive(String email, Boolean active) throws EmailNotFoundException;

    void updatePropertyNotLocked(String email, Boolean notLocked) throws EmailNotFoundException;

    User addContentToMarkedCourseContents(Long userId, Long onlineCourseContentId) throws UserNotFoundException, CourseContentNotFoundException;

    User removeContentFromMarkedCourseContents(Long userId, Long onlineCourseContentId) throws UserNotFoundException, CourseContentNotFoundException;

    boolean checkIfMarkedCourseContent(Long userId, Long onlineCourseContentId);

    Page<OnlineCourse> getSubscribedOnlineCoursesByUserId(Long userId, Pageable pageable) throws UserNotFoundException;

    long countSubscribedOnlineCoursesByUserId(Long userId);

    User addCourseToSubscribedOnlineCourses(Long userId, Long onlineCourseId) throws OnlineCourseNotFoundException, UserNotFoundException;

    User removeCourseFromSubscribedOnlineCourses(Long userId, Long onlineCourseId) throws OnlineCourseNotFoundException, UserNotFoundException;

    //User findUserByUsername(String email);

    User findUserByUserId(String userId) throws UserNotFoundException;

    User findUserByEmail(String email);

    List<User> getUsers();

    Page<User> findAll(String searchParam, Pageable pageable) throws UserNotFoundException;

    long getTotal();

    Set<Subject> getUserSubjectInterests(Long userId) throws UserNotFoundException;

    User addInterestToUserInterests(Long userId, Long postId) throws SubjectNotFoundException, UserNotFoundException;

    User removeInterestFromUserInterests(Long userId, Long postId) throws SubjectNotFoundException, UserNotFoundException;

}
