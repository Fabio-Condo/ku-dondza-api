package com.fabiocondo.service.impl;

import com.fabiocondo.aws.model.S3UploadResponse;
import com.fabiocondo.aws.service.AmazonS3Service;
import com.fabiocondo.enumeration.Role;
import com.fabiocondo.exception.domain.EmailExistException;
import com.fabiocondo.exception.domain.EmailNotFoundException;
import com.fabiocondo.exception.domain.UserNotFoundException;
import com.fabiocondo.exception.domain.UsernameExistException;
import com.fabiocondo.repository.UserRepository;
import com.fabiocondo.security.User;
import com.fabiocondo.security.UserPrincipal;
import com.fabiocondo.security.service.EmailService;
import com.fabiocondo.security.service.LoginAttemptService;
import com.fabiocondo.service.UserService;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.MessagingException;
import javax.transaction.Transactional;
import java.io.IOException;
import java.util.Date;
import java.util.List;

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

    @Autowired
    public UserServiceImpl(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder, LoginAttemptService loginAttemptService, EmailService emailService, AmazonS3Service amazonS3Service) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.loginAttemptService = loginAttemptService;
        this.emailService = emailService;
        this.amazonS3Service = amazonS3Service;
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
    public User findUserByUsername(String username) {
        return userRepository.findUserByUsername(username);
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
    public List<User> getUsers() {
        return userRepository.findAll();
    }

}
