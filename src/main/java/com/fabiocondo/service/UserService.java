package com.fabiocondo.service;

import com.fabiocondo.domain.*;
import com.fabiocondo.enumeration.Plan;
import com.fabiocondo.enumeration.UserType;
import com.fabiocondo.exception.domain.*;
import com.fabiocondo.repository.filter.UserFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.MessagingException;
import javax.transaction.Transactional;
import java.io.IOException;
import java.util.List;
import java.util.Set;

public interface UserService {

    List<User> getAllInstrutores();

    User register(String fullName, String email, String profileImageUrl) throws UserNotFoundException, MessagingException, EmailExistException;

    User updateUserProfile(String currentEmail, String fullName, String newEmail, String newBio, String role, boolean isNonLocked, boolean isActive, MultipartFile profileImage) throws UserNotFoundException, EmailExistException, IOException;

    User addNewUser(String fullName, String email, String role, UserType userType, boolean isNonLocked, boolean isActive, MultipartFile profileImage) throws UserNotFoundException, EmailExistException, MessagingException, IOException;

    User updateUser(String currentEmail, String fullName, String newEmail, String role, UserType userType, boolean isNonLocked, boolean isActive, MultipartFile profileImage) throws UserNotFoundException, EmailExistException, IOException;

    User update(User user, Long id) throws UserNotFoundException;

    User updateUserProfilePhoto(String currentEmail, MultipartFile profileImage) throws IOException, EmailNotFoundException;

    User updateUserProfileCoverPhoto(String currentEmail, MultipartFile profileImage) throws IOException, EmailNotFoundException;

    void resetPassword(String email) throws MessagingException, EmailNotFoundException;

    void deleteUser(String email) throws IOException;

    User updateProfileImage(String email, MultipartFile profileImage) throws EmailExistException, IOException, UserNotFoundException;

    User activatePlan(Long userId, Plan plan, Long walletId) throws UserNotFoundException, WalletNotFoundException, PaymentException, MessagingException;

    boolean isPlanActive(User user, Plan plan);

    void cancelSubscription(Long userId);

    void updatePropertyActive(String email, Boolean active) throws EmailNotFoundException;

    void updatePropertyNotLocked(String email, Boolean notLocked) throws EmailNotFoundException;

    User toggleTopicContentMarkedStatus(Long userId, Long topicContentId) throws UserNotFoundException, ContentNotFoundException;

    @Transactional
    Question toggleSaveQuestion(Long userId, Long questionId) throws QuestionNotFoundException;

    boolean checkIfSavedQuestion(Long questionId, Long currentUserId);

    User findUserByUserId(String userId) throws UserNotFoundException;

    User findUserByEmail(String email);

    List<User> getUsers();

    Page<User> findAll(String searchParam, Pageable pageable) throws UserNotFoundException;

    Page<User> filter(UserFilter userFilter, Pageable pageable);

    long getTotal();

    Set<Subject> getUserSubjectInterests(Long userId) throws UserNotFoundException;

    User addInterestToUserInterests(Long userId, Long postId) throws SubjectNotFoundException, UserNotFoundException;

    User removeInterestFromUserInterests(Long userId, Long postId) throws SubjectNotFoundException, UserNotFoundException;

}
