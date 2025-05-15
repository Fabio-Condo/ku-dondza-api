package com.fabiocondo.service.impl;

import com.fabiocondo.aws.model.S3UploadResponse;
import com.fabiocondo.aws.service.AmazonS3Service;
import com.fabiocondo.domain.*;
import com.fabiocondo.enumeration.Plan;
import com.fabiocondo.enumeration.Role;
import com.fabiocondo.enumeration.UserType;
import com.fabiocondo.exception.domain.*;
import com.fabiocondo.repository.*;
import com.fabiocondo.repository.filter.UserFilter;
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
import static com.fabiocondo.enumeration.Role.ROLE_USER;
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
    private final SubjectRepository subjectRepository;
    private final CourseRepository courseRepository;
    private final ArticleRepository articleRepository;
    private final ContentRepository contentRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder, LoginAttemptService loginAttemptService, EmailService emailService, AmazonS3Service amazonS3Service, SubjectRepository subjectRepository, CourseRepository courseRepository, ArticleRepository articleRepository, ContentRepository contentRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.loginAttemptService = loginAttemptService;
        this.emailService = emailService;
        this.amazonS3Service = amazonS3Service;
        this.subjectRepository = subjectRepository;
        this.courseRepository = courseRepository;
        this.articleRepository = articleRepository;
        this.contentRepository = contentRepository;
    }

    public Page<User> searchUsers(String query, Pageable pageable) {
        return userRepository.searchByQuery(query, pageable);
    }

    @Override
    public List<User> getAllInstrutores() {
        return userRepository.findByUserType(UserType.INSTRUTOR);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findUserByEmail(email);
        if (user == null) {
            logger.error(NO_USER_FOUND_BY_EMAIL + email);
            throw new UsernameNotFoundException(NO_USER_FOUND_BY_EMAIL + email);
        } else {
            validateLoginAttempt(user);
            user.setLastLoginDateDisplay(user.getLastLoginDate());
            user.setLastLoginDate(new Date());
            userRepository.save(user);
            UserPrincipal userPrincipal = new UserPrincipal(user);
            logger.info(FOUND_USER_BY_EMAIL + email);
            return userPrincipal;
        }
    }

    public User findById(Long id) throws UserNotFoundException {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("No user found by id: " + id));
    }

    @Override
    public User register(String fullName, String email,  String profileImageUrl) throws UserNotFoundException, MessagingException, EmailExistException {
        validateNewEmail(EMPTY, email);
        User user = new User();
        user.setPlan(Plan.PREMIUM);
        user.setUserType(UserType.STUDENT);
        user.setUserId(generateUserId());
        String password = generatePassword();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setProfileImageUrl(profileImageUrl);
        user.setJoinDate(new Date());
        user.setPassword(encodePassword(password));
        user.setActive(true);
        user.setNotLocked(true);
        user.setRole(ROLE_USER.name());
        user.setAuthorities(ROLE_USER.getAuthorities());
        userRepository.save(user);
        logger.info("New user password (register): " + password);
        //emailService.sendNewPasswordEmail(fullName, email, password);
        return user;
    }

    @Override
    public User addNewUser(String firstName, String email, String role, UserType userType, boolean isNonLocked, boolean isActive, MultipartFile profileImage) throws UserNotFoundException, EmailExistException, MessagingException {
        validateNewEmail(EMPTY, email);

        logger.info("Uploading file: " + profileImage.getOriginalFilename());
        S3UploadResponse s3UploadResponse = amazonS3Service.uploadFile(profileImage, BUCKET_NAME);

        // Adicionar funcao que diminue o tamanho da imagem
        User user = new User();
        String password = generatePassword();
        user.setUserId(generateUserId());
        user.setFullName(firstName);
        user.setJoinDate(new Date());
        user.setEmail(email);
        user.setPassword(encodePassword(password));
        //user.setActive(isActive);
        //user.setNotLocked(isNonLocked);
        user.setActive(true);
        user.setNotLocked(true);
        user.setUserType(userType);
        user.setRole(getRoleEnumName(role).name());
        user.setAuthorities(getRoleEnumName(role).getAuthorities());
        user.setProfileImageUrl(s3UploadResponse.getFileUrl());
        userRepository.save(user);
        emailService.sendOtpCodeEmail(email, password);
        logger.info("New user password: " + password);
        return user;
    }

    @Override
    public User updateUser(String currentEmail, String newFullName, String newEmail, String role, UserType userType, boolean isNonLocked, boolean isActive, MultipartFile profileImage) throws UserNotFoundException, EmailExistException {
        User currentUser = validateNewEmail(currentEmail, newEmail);
        // Adicionar funcao que diminue o tamanho da imagem
        currentUser.setFullName(newFullName);
        currentUser.setEmail(newEmail);
        //currentUser.setActive(isActive);
        //currentUser.setNotLocked(isNonLocked);
        currentUser.setUserType(userType);
        currentUser.setActive(true);
        currentUser.setNotLocked(true);
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
    public User updateUserProfile(String currentEmail, String newFullName, String newEmail, String newBio, String role, boolean isNonLocked, boolean isActive, MultipartFile profileImage) throws UserNotFoundException, EmailExistException {
        User currentUser = validateNewEmail(currentEmail, newEmail);
        // Adicionar funcao que diminue o tamanho da imagem
        currentUser.setFullName(newFullName);
        currentUser.setEmail(newEmail);
        currentUser.setBio(newBio);
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
    public User update(User user, Long id) throws UserNotFoundException {
        User existUser = findById(id);
        BeanUtils.copyProperties(user, existUser, "id", "password");
        logger.info("Updating user: " + user.getFullName());
        return userRepository.save(existUser);
    }

    @Override
    public User updateUserProfilePhoto(String currentEmail, MultipartFile profileImage) throws IOException, EmailNotFoundException {
        // Adicionar funcao que diminue o tamanho da imagem

        User currentUser = userRepository.findUserByEmail(currentEmail);
        if (currentUser == null) {
            throw new EmailNotFoundException(NO_USER_FOUND_BY_EMAIL + currentEmail);

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
    public User updateUserProfileCoverPhoto(String currentEmail, MultipartFile coverImage) throws IOException, EmailNotFoundException {
        // Adicionar funcao que diminue o tamanho da imagem

        User currentUser = userRepository.findUserByEmail(currentEmail);
        if (currentUser == null) {
            throw new EmailNotFoundException(NO_USER_FOUND_BY_EMAIL + currentEmail);
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
        emailService.sendOtpCodeEmail(user.getEmail(), password);
    }

    @Override
    public void deleteUser(String email) {
        User user = userRepository.findUserByEmail(email);
        userRepository.deleteById(user.getId());
    }

    @Override
    public User updateProfileImage(String email, MultipartFile profileImage) throws EmailExistException, UserNotFoundException {
        User user = validateNewEmail(email, null);
        //saveProfileImage(user, profileImage);
        return user;
    }

    @Override
    public void updatePropertyActive(String email, Boolean active) {
        User userSaved = findUserByEmail(email);
        userSaved.setActive(active);
        logger.info("Updating user: " + userSaved.getFullName());
        userRepository.save(userSaved);
    }

    @Override
    public void updatePropertyNotLocked(String email, Boolean notLocked) {
        User userSaved = findUserByEmail(email);
        userSaved.setNotLocked(notLocked);
        logger.info("Updating user: " + userSaved.getFullName());
        userRepository.save(userSaved);
    }

    @Override
    public Page<User> findAll(String searchParam, Pageable pageable) throws UserNotFoundException {
        return userRepository.findByAnyProperty(searchParam, pageable);
    }

    @Override
    public Page<User> filter(UserFilter userFilter, Pageable pageable) {
        return userRepository.filter(userFilter, pageable);
    }

    @Override
    public long getTotal(){
        logger.info("Total users: " + userRepository.count());
        return userRepository.count();
    }

    public User save(User user){
        return userRepository.save(user);
    }

    @Override
    public Set<Subject> getUserSubjectInterests(Long userId) throws UserNotFoundException {
        User user = findById(userId);
        if (user == null) {
            throw new UserNotFoundException("User not found by id: " + userId);
        }
        return user.getSubjectsInterests();
    }

    @Override
    public User addInterestToUserInterests(Long userId, Long interestId) throws SubjectNotFoundException, UserNotFoundException {
        User user = findById(userId);
        Optional<Subject> optionalInterest = subjectRepository.findById(interestId);
        if (!optionalInterest.isPresent()){
            throw new SubjectNotFoundException("Interest not found by id: " + interestId);
        }
        user.getSubjectsInterests().add(optionalInterest.get());
        return userRepository.save(user);
    }

    @Override
    public User removeInterestFromUserInterests(Long userId, Long interestId) throws SubjectNotFoundException, UserNotFoundException {
        User user = findById(userId);
        Optional<Subject> optionalInterest = subjectRepository.findById(interestId);
        if (!optionalInterest.isPresent()) {
            throw new SubjectNotFoundException("Interest not found by id: " + interestId);
        }
        user.getSubjectsInterests().remove(optionalInterest.get());
        return userRepository.save(user);
    }

    @Override
    public User toggleContentMarkedStatus(Long userId, Long onlineCourseContentId) throws UserNotFoundException, ContentNotFoundException {
        User user = findById(userId);

        Content content = contentRepository.findById(onlineCourseContentId)
                .orElseThrow(() -> new ContentNotFoundException("No Course Content found by id: " + onlineCourseContentId));

        Set<Content> markedContents = user.getMarkedContents();

        if (markedContents.contains(content)) {
            markedContents.remove(content);
        } else {
            markedContents.add(content);
        }

        return userRepository.save(user);
    }

    @Override
    public Page<Course> getSubscribedOnlineCoursesByUserId(Long userId, Pageable pageable) throws UserNotFoundException {
        User user = findById(userId);
        return userRepository.findSubscribedCoursesByUserId(user.getId(), pageable);
    }

    @Override
    @Transactional
    public Article toggleSaveArticle(Long userId, Long articleId) throws UserNotFoundException, ArticleNotFoundException {
        User user = getAuthenticatedUser();

        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new ArticleNotFoundException("Article not find"));

        Set<Article> savedArticles = user.getSavedArticles();

        if (savedArticles.contains(article)) {
            savedArticles.remove(article);
        } else {
            savedArticles.add(article);
        }

        userRepository.save(user); // atualiza a relação

        return article;
    }

    @Override
    public boolean checkIfSaved(Long articleId) throws UserNotFoundException, ArticleNotFoundException {
        User user = getAuthenticatedUser();

        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new ArticleNotFoundException("Article not find"));

        return user.getSavedArticles().contains(article);
    }


    private void validateLoginAttempt(User user) {
        if(user.isNotLocked()) {
            if(loginAttemptService.hasExceededMaxAttempts(user.getEmail())) {
                user.setNotLocked(false);
            } else {
                user.setNotLocked(true);
            }
        } else {
            loginAttemptService.evictUserFromLoginAttemptCache(user.getEmail());
        }
    }

    private User validateNewEmail(String currentEmail, String newEmail) throws UserNotFoundException, EmailExistException {
        User userByNewEmail = findUserByEmail(newEmail);
        if(StringUtils.isNotBlank(currentEmail)) {
            User currentUser = findUserByEmail(currentEmail);
            if(currentUser == null) {
                throw new UserNotFoundException(NO_USER_FOUND_BY_EMAIL + currentEmail);
            }
            if(userByNewEmail != null && !currentUser.getId().equals(userByNewEmail.getId())) {
                throw new EmailExistException(EMAIL_ALREADY_EXISTS);
            }
            return currentUser;
        } else {
            if(userByNewEmail != null) {
                throw new EmailExistException(EMAIL_ALREADY_EXISTS);
            }
            return null;
        }
    }

    @Override
    public User findUserByUserId(String userId) throws UserNotFoundException {
        return userRepository.findUserByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException("No user found by id: " + userId));
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
        return RandomStringUtils.randomAlphanumeric(10);
    }

    @Override
    public List<User> getUsers() {
        return userRepository.findAll();
    }

    public User getAuthenticatedUser() throws UserNotFoundException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User user = userRepository.findUserByEmail(username);
        if(user == null){
            throw new UserNotFoundException(NO_USER_FOUND_BY_EMAIL + username);
        }
        logger.info(FOUND_USER_BY_EMAIL + username);
        return userRepository.findUserByEmail(username);
    }

}
