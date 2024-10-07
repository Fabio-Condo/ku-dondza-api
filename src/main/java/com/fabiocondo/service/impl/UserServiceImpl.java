package com.fabiocondo.service.impl;

import com.fabiocondo.aws.model.S3UploadResponse;
import com.fabiocondo.aws.service.AmazonS3Service;
import com.fabiocondo.domain.*;
import com.fabiocondo.enumeration.Role;
import com.fabiocondo.exception.domain.*;
import com.fabiocondo.repository.InterestRepository;
import com.fabiocondo.repository.OnlineCourseRepository;
import com.fabiocondo.repository.PostRepository;
import com.fabiocondo.repository.UserRepository;
import com.fabiocondo.security.service.EmailService;
import com.fabiocondo.security.service.LoginAttemptService;
import com.fabiocondo.service.UserService;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.MessagingException;
import javax.transaction.Transactional;
import java.io.IOException;
import java.util.*;

import static com.fabiocondo.constant.UserImplConstant.*;
import static com.fabiocondo.enumeration.Role.ROLE_SUPER_ADMIN;
import static org.apache.commons.lang3.StringUtils.EMPTY;

@Service
@Transactional
@Qualifier("userDetailsService")  
public class UserServiceImpl implements UserService, UserDetailsService {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private static final String BUCKET_NAME = "b-tests-bucket";
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final LoginAttemptService loginAttemptService;
    private final EmailService emailService;
    private final AmazonS3Service amazonS3Service;
    private final PostRepository postRepository;
    private final InterestRepository interestRepository;
    private final OnlineCourseRepository onlineCourseRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder, LoginAttemptService loginAttemptService, EmailService emailService, AmazonS3Service amazonS3Service, PostRepository postRepository, InterestRepository interestRepository, OnlineCourseRepository onlineCourseRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.loginAttemptService = loginAttemptService;
        this.emailService = emailService;
        this.amazonS3Service = amazonS3Service;
        this.postRepository = postRepository;
        this.interestRepository = interestRepository;
        this.onlineCourseRepository = onlineCourseRepository;
    }

    public Page<User> searchUsers(String query, Pageable pageable) {
        return userRepository.searchByQuery(query, pageable);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findUserByUsername(username);
        if (user == null) {
            logger.error(NO_USER_FOUND_BY_USERNAME + username);
            throw new UsernameNotFoundException(NO_USER_FOUND_BY_USERNAME + username);
        } else {
            validateLoginAttempt(user);
            user.setLastLoginDateDisplay(user.getLastLoginDate());
            user.setLastLoginDate(new Date());
            userRepository.save(user);
            UserPrincipal userPrincipal = new UserPrincipal(user);
            logger.info(FOUND_USER_BY_USERNAME + username);
            return userPrincipal;
        }
    }

    public User findById(Long id) throws UsernameNotFoundException {
        return userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("No user found by id: " + id));
    }

    @Override
    public User register(String firstName, String lastName, String username, String email) throws UserNotFoundException, MessagingException, UsernameExistException, EmailExistException {
        validateNewUsernameAndEmail(EMPTY, username, email);
        User user = new User();
        user.setUserId(generateUserId());
        String password = generatePassword();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setUsername(username);
        user.setEmail(email);
        user.setJoinDate(new Date());
        user.setPassword(encodePassword(password));
        user.setActive(true);
        user.setNotLocked(true);
        user.setRole(ROLE_SUPER_ADMIN.name());
        user.setAuthorities(ROLE_SUPER_ADMIN.getAuthorities());
        userRepository.save(user);
        logger.info("New user password (register): " + password);
        emailService.sendNewPasswordEmail(firstName, username, password, email);
        return user;
    }

    @Override
    public User addNewUser(String firstName, String lastName, String username, String email, String role, boolean isNonLocked, boolean isActive, MultipartFile profileImage) throws UserNotFoundException, UsernameExistException, EmailExistException, MessagingException {
        validateNewUsernameAndEmail(EMPTY, username, email);

        logger.info("Uploading file: " + profileImage.getOriginalFilename());
        S3UploadResponse s3UploadResponse = amazonS3Service.uploadFile(profileImage, BUCKET_NAME);

        // Adicionar funcao que diminue o tamanho da imagem
        User user = new User();
        String password = generatePassword();
        user.setUserId(generateUserId());
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setJoinDate(new Date());
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(encodePassword(password));
        user.setActive(isActive);
        user.setNotLocked(isNonLocked);
        user.setRole(getRoleEnumName(role).name());
        user.setAuthorities(getRoleEnumName(role).getAuthorities());
        user.setProfileImageUrl(s3UploadResponse.getFileUrl());
        userRepository.save(user);
        emailService.sendNewPasswordEmail(firstName, username, password, email);
        logger.info("New user password: " + password);
        return user;
    }

    @Override
    public User updateUser(String currentUsername, String newFirstName, String newLastName, String newUsername, String newEmail, String role, boolean isNonLocked, boolean isActive, MultipartFile profileImage) throws UserNotFoundException, UsernameExistException, EmailExistException {
        User currentUser = validateNewUsernameAndEmail(currentUsername, newUsername, newEmail);
        // Adicionar funcao que diminue o tamanho da imagem
        currentUser.setFirstName(newFirstName);
        currentUser.setLastName(newLastName);
        currentUser.setUsername(newUsername);
        currentUser.setEmail(newEmail);
        currentUser.setActive(isActive);
        currentUser.setNotLocked(isNonLocked);
        currentUser.setRole(getRoleEnumName(role).name());
        currentUser.setAuthorities(getRoleEnumName(role).getAuthorities());

        // Se um novo arquivo é fornecido, atualiza o arquivo no serviço Amazon S3 e atualiza o nome e a URL do arquivo
        if (profileImage != null) {
            if (currentUser.getFileName() != null) {
                logger.info("Deleting file: " + currentUser.getFileName());
                amazonS3Service.deleteFile(currentUser.getFileName(), BUCKET_NAME);
            }
            S3UploadResponse s3UploadResponse = amazonS3Service.uploadFile(profileImage, BUCKET_NAME);
            currentUser.setProfileImageUrl(s3UploadResponse.getFileUrl());
            currentUser.setFileName(profileImage.getOriginalFilename());
        }

        userRepository.save(currentUser);
        return currentUser;
    }

    @Override
    public User updateUserProfile(String currentUsername, String newFirstName, String newLastName, String newUsername, String newEmail, String newInstitution, String newBio, String newCourse, String role, boolean isNonLocked, boolean isActive, MultipartFile profileImage) throws UserNotFoundException, UsernameExistException, EmailExistException {
        User currentUser = validateNewUsernameAndEmail(currentUsername, newUsername, newEmail);
        // Adicionar funcao que diminue o tamanho da imagem
        currentUser.setFirstName(newFirstName);
        currentUser.setLastName(newLastName);
        currentUser.setUsername(newUsername);
        currentUser.setEmail(newEmail);
        currentUser.setInstitution(newInstitution);
        currentUser.setBio(newBio);
        currentUser.setCourse(newCourse);
        currentUser.setActive(isActive);
        currentUser.setNotLocked(isNonLocked);
        currentUser.setRole(getRoleEnumName(role).name());
        currentUser.setAuthorities(getRoleEnumName(role).getAuthorities());

        // Se um novo arquivo é fornecido, atualiza o arquivo no serviço Amazon S3 e atualiza o nome e a URL do arquivo
        if (profileImage != null) {
            if (currentUser.getFileName() != null) {
                logger.info("Deleting file: " + currentUser.getFileName());
                amazonS3Service.deleteFile(currentUser.getFileName(), BUCKET_NAME);
            }
            S3UploadResponse s3UploadResponse = amazonS3Service.uploadFile(profileImage, BUCKET_NAME);
            currentUser.setProfileImageUrl(s3UploadResponse.getFileUrl());
            currentUser.setFileName(profileImage.getOriginalFilename());
        }

        userRepository.save(currentUser);
        return currentUser;
    }

    @Override
    public User update(User user, Long id) {
        User existUser = findById(id);
        BeanUtils.copyProperties(user, existUser, "id", "password");
        logger.info("Updating user: " + user.getFirstName());
        return userRepository.save(existUser);
    }

    @Override
    public User updateUserProfilePhoto(String currentUsername, MultipartFile profileImage) throws IOException {
        // Adicionar funcao que diminue o tamanho da imagem

        User currentUser = userRepository.findUserByUsername(currentUsername);
        if (currentUser == null) {
            throw new UsernameNotFoundException(NO_USER_FOUND_BY_USERNAME + currentUsername);
        }

        if (profileImage == null || profileImage.isEmpty()) {
            throw new IOException("The file is null or empty");
        }

        // Deleta o arquivo antigo do S3
        if (currentUser.getFileName() != null) {
            logger.info("Deleting file: " + currentUser.getFileName());
            amazonS3Service.deleteFile(currentUser.getFileName(), BUCKET_NAME);
        }

        S3UploadResponse s3UploadResponse = amazonS3Service.uploadFile(profileImage, BUCKET_NAME);
        currentUser.setProfileImageUrl(s3UploadResponse.getFileUrl());
        currentUser.setFileName(profileImage.getOriginalFilename());

        userRepository.save(currentUser);
        return currentUser;
    }

    @Override
    public User updateUserProfileCoverPhoto(String currentUsername, MultipartFile coverImage) throws IOException {
        // Adicionar funcao que diminue o tamanho da imagem

        User currentUser = userRepository.findUserByUsername(currentUsername);
        if (currentUser == null) {
            throw new UsernameNotFoundException(NO_USER_FOUND_BY_USERNAME + currentUsername);
        }

        if (coverImage == null || coverImage.isEmpty()) {
            throw new IOException("The file is null or empty");
        }

        // Deleta o arquivo antigo do S3
        if (currentUser.getFileNameCoverImage() != null) {
            logger.info("Deleting file: " + currentUser.getFileNameCoverImage());
            amazonS3Service.deleteFile(currentUser.getFileNameCoverImage(), BUCKET_NAME);
        }

        S3UploadResponse s3UploadResponse = amazonS3Service.uploadFile(coverImage, BUCKET_NAME);
        currentUser.setProfileCoverImageUrl(s3UploadResponse.getFileUrl());
        currentUser.setFileNameCoverImage(coverImage.getOriginalFilename());

        userRepository.save(currentUser);
        return currentUser;
    }

    @Override
    public void resetPassword(String email) throws MessagingException, EmailNotFoundException {
        User user = userRepository.findUserByEmail(email);
        if (user == null) {
            throw new EmailNotFoundException(NO_USER_FOUND_BY_EMAIL + email);
        }
        String password = generatePassword();
        user.setPassword(encodePassword(password));
        userRepository.save(user);
        logger.info("New user password: " + password);
        emailService.sendNewPasswordEmail(user.getFirstName(), user.getUsername(), password, user.getEmail());
    }

    @Override
    public void deleteUser(String username) {
        User user = userRepository.findUserByUsername(username);
        userRepository.deleteById(user.getId());
    }

    @Override
    public User updateProfileImage(String username, MultipartFile profileImage) throws UsernameExistException, EmailExistException, IOException, UserNotFoundException {
        User user = validateNewUsernameAndEmail(username, null, null);
        //saveProfileImage(user, profileImage);
        return user;
    }

    @Override
    public void updatePropertyActive(String username, Boolean active) throws UsernameNotFoundException {
        User userSaved = findUserByUsername(username);
        userSaved.setActive(active);
        logger.info("Updating user: " + userSaved.getFirstName());
        userRepository.save(userSaved);
    }

    @Override
    public void updatePropertyNotLocked(String username, Boolean notLocked) throws UsernameNotFoundException {
        User userSaved = findUserByUsername(username);
        userSaved.setNotLocked(notLocked);
        logger.info("Updating user: " + userSaved.getFirstName());
        userRepository.save(userSaved);
    }

    @Override
    public Page<User> findAll(String name, Pageable pageable) throws UserNotFoundException {
        return userRepository.findByAnyProperty(name, pageable);
    }

    public User save(User user){
        return userRepository.save(user);
    }

    @Override
    public User addInterestToUserInterests(Long userId, Long interestId) throws InterestNotFoundException {
        User user = findById(userId);
        Optional<Interest> optionalInterest = interestRepository.findById(interestId);
        if (!optionalInterest.isPresent()){
            throw new InterestNotFoundException("Interest not found by id: " + interestId);
        }
        user.getInterests().add(optionalInterest.get());
        return userRepository.save(user);
    }

    @Override
    public User removeInterestFromUserInterests(Long userId, Long interestId) throws InterestNotFoundException {
        User user = findById(userId);
        Optional<Interest> optionalInterest = interestRepository.findById(interestId);
        if (!optionalInterest.isPresent()) {
            throw new InterestNotFoundException("Interest not found by id: " + interestId);
        }
        user.getInterests().remove(optionalInterest.get());
        return userRepository.save(user);
    }

    @Override
    public User addPostToSavedPosts(Long userId, Long postId) throws PostNotFoundException {
        User user = findById(userId);
       Optional<Post> optionalPost = postRepository.findById(postId);
       if (!optionalPost.isPresent()){
           throw new PostNotFoundException("Post not found by id: " + postId);
       }
        user.getSavedPosts().add(optionalPost.get());
        return userRepository.save(user);
    }

    @Override
    public User removePostFromSavedPosts(Long userId, Long postId) throws PostNotFoundException {
        User user = findById(userId);
        Optional<Post> optionalPost = postRepository.findById(postId);
        if (!optionalPost.isPresent()) {
            throw new PostNotFoundException("Post not found by id: " + postId);
        }
        user.getSavedPosts().remove(optionalPost.get());
        return userRepository.save(user);
    }

    @Override
    public List<Post> getSavedPosts(Long userId) throws UserNotFoundException {
        User user = findById(userId);
        if (user == null) {
            throw new UserNotFoundException("User not found by id: " + userId);
        }
        return user.getSavedPosts();
    }

    @Override
    public Page<Post> getSavedPostsPaginated(Long id, Pageable pageable) throws UserNotFoundException {
        User user = findById(id);
        if (user == null) {
            throw new UserNotFoundException("User not found by id: " + id);
        }

        List<Post> savedPosts = user.getSavedPosts();
        if (savedPosts == null) {
            return Page.empty(pageable);
        }

        List<Post> savedPostsList = new ArrayList<>(savedPosts);
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), savedPostsList.size());
        return new PageImpl<>(savedPostsList.subList(start, end), pageable, savedPostsList.size());
    }

    @Override
    public boolean doesUserSavedPost(Long userId, Long postId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return false;
        }
        Optional<Post> post = postRepository.findById(postId);
        if (!post.isPresent()) {
            return false;
        }
        return user.getSavedPosts().contains(post.get());
    }

    @Override
    public List<User> getFriendRequests() throws UserNotFoundException {
        User user = getAuthenticatedUser();
        return user.getFriendRequests();
    }

    @Override
    public void sendFriendRequest(User friend) throws UserNotFoundException {
        User user = getAuthenticatedUser();
        User passedUser = findById(friend.getId());
        passedUser.getFriendRequests().add(user); // Saving the request in user I passed
        userRepository.save(passedUser);
    }

    @Override
    public User acceptFriendRequest(Long friendId) throws UserNotFoundException {
        User user = getAuthenticatedUser();
        User friend = findById(friendId);
        user.getFriendRequests().remove(friend);
        user.getFriends().add(friend);
        friend.getFriends().add(user);
        userRepository.saveAll(Arrays.asList(user, friend));
        return friend;
    }

    @Override
    public void rejectFriendRequest(Long friendId) throws UserNotFoundException {
        User user = getAuthenticatedUser();
        User friend = findById(friendId);
        user.getFriendRequests().remove(friend);
        userRepository.save(user);
    }

    @Override
    public List<User> getFriends() throws UserNotFoundException {
        User user = getAuthenticatedUser();
        return user.getFriends();
    }

    @Override
    public void removeFriend(Long friendId) throws UserNotFoundException {
        User user = getAuthenticatedUser();
        User friend = findById(friendId);
        user.getFriends().remove(friend);
        friend.getFriends().remove(user);
        userRepository.saveAll(Arrays.asList(user, friend));
    }

    private void validateLoginAttempt(User user) {
        if(user.isNotLocked()) {
            if(loginAttemptService.hasExceededMaxAttempts(user.getUsername())) {
                user.setNotLocked(false);
            } else {
                user.setNotLocked(true);
            }
        } else {
            loginAttemptService.evictUserFromLoginAttemptCache(user.getUsername());
        }
    }

    @Override
    public User addCourseToSubscribedOnlineCourses(Long userId, Long onlineCourseId) throws CourseNotFoundException {
        User user = findById(userId);
        Optional<OnlineCourse> optionalCourse = onlineCourseRepository.findById(onlineCourseId);
        if (!optionalCourse.isPresent()){
            throw new CourseNotFoundException("Online Course not found by id: " + onlineCourseId);
        }
        user.getSubscribedOnlineCourses().add(optionalCourse.get());
        return userRepository.save(user);
    }

    @Override
    public User removeCourseFromSubscribedOnlineCourses(Long userId, Long onlineCourseId) throws CourseNotFoundException {
        User user = findById(userId);
        Optional<OnlineCourse> optionalCourse = onlineCourseRepository.findById(onlineCourseId);
        if (!optionalCourse.isPresent()) {
            throw new CourseNotFoundException("Online Curse not found by id: " + onlineCourseId);
        }
        user.getSubscribedOnlineCourses().remove(optionalCourse.get());
        return userRepository.save(user);
    }

    @Override
    public boolean doesUserSubscribedOnlineCourse(Long userId, Long onlineCourseId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return false;
        }
        Optional<OnlineCourse> course = onlineCourseRepository.findById(onlineCourseId);
        if (!course.isPresent()) {
            return false;
        }
        return user.getSubscribedOnlineCourses().contains(course.get());
    }

    private User validateNewUsernameAndEmail(String currentUsername, String newUsername, String newEmail) throws UserNotFoundException, UsernameExistException, EmailExistException {
        User userByNewUsername = findUserByUsername(newUsername);
        User userByNewEmail = findUserByEmail(newEmail);
        if(StringUtils.isNotBlank(currentUsername)) {
            User currentUser = findUserByUsername(currentUsername);
            if(currentUser == null) {
                throw new UserNotFoundException(NO_USER_FOUND_BY_USERNAME + currentUsername);
            }
            if(userByNewUsername != null && !currentUser.getId().equals(userByNewUsername.getId())) {
                throw new UsernameExistException(USERNAME_ALREADY_EXISTS);
            }
            if(userByNewEmail != null && !currentUser.getId().equals(userByNewEmail.getId())) {
                throw new EmailExistException(EMAIL_ALREADY_EXISTS);
            }
            return currentUser;
        } else {
            if(userByNewUsername != null) {
                throw new UsernameExistException(USERNAME_ALREADY_EXISTS);
            }
            if(userByNewEmail != null) {
                throw new EmailExistException(EMAIL_ALREADY_EXISTS);
            }
            return null;
        }
    }

    @Override
    public User findUserByUsername(String username) {
        return userRepository.findUserByUsername(username);
    }

    @Override
    public User findUserByUserId(String userId) {
        return userRepository.findUserByUserId(userId);
    }

    @Override
    public User findUserByEmail(String email) {
        return userRepository.findUserByEmail(email);
    }

    private Role getRoleEnumName(String role) {
        return Role.valueOf(role.toUpperCase());
    }

    private String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }

    private String generatePassword() {
        return RandomStringUtils.randomAlphanumeric(10);
    }

    private String generateUserId() {
        return RandomStringUtils.randomNumeric(10);
    }

    @Override
    public List<User> getUsers() {
        return userRepository.findAll();
    }

    public User getAuthenticatedUser() throws UserNotFoundException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User user = userRepository.findUserByUsername(username);
        if(user == null){
            throw new UserNotFoundException(NO_USER_FOUND_BY_USERNAME + username);
        }
        logger.info(FOUND_USER_BY_USERNAME + username);
        return userRepository.findUserByUsername(username);
    }

}
