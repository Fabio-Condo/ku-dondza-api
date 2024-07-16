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

    void resetPassword(String email) throws MessagingException, EmailNotFoundException;

    void deleteUser(String username) throws IOException;

    User updateProfileImage(String username, MultipartFile profileImage) throws UsernameExistException, EmailExistException, IOException, UserNotFoundException;

    void updatePropertyActive(String username, Boolean active) throws UsernameNotFoundException;

    void updatePropertyNotLocked(String username, Boolean notLocked) throws UsernameNotFoundException;

    User findUserByUsername(String username);

    User findUserByUserId(String userId);

    User findUserByEmail(String email);

    List<User> getUsers();

    Page<User> findAll(String name, Pageable pageable) throws UserNotFoundException;

    User addPostToSavedPosts(Long userId, Long postId) throws PostNotFoundException;

    User removePostFromSavedPosts(Long userId, Long postId) throws PostNotFoundException;

    List<Post> getSavedPosts(Long id) throws UserNotFoundException;

    Page<Post> getSavedPostsPaginated(Long id, Pageable pageable) throws UserNotFoundException;

    boolean doesUserSavedPost(Long userId, Long postId);

    List<User> getFriendRequests() throws UserNotFoundException;

    void sendFriendRequest(User friend) throws UserNotFoundException;

    User acceptFriendRequest(Long friendId) throws UserNotFoundException;

    void rejectFriendRequest(Long friendId) throws UserNotFoundException;

    List<User> getFriends() throws UserNotFoundException;

    void removeFriend(Long friendId) throws UserNotFoundException;
}
