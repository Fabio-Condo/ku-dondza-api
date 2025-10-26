package com.fabiocondo.service.impl;

import com.fabiocondo.aws.model.S3UploadResponse;
import com.fabiocondo.aws.service.AmazonS3Service;
import com.fabiocondo.domain.*;
import com.fabiocondo.enumeration.*;
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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.MessagingException;
import javax.transaction.Transactional;
import java.io.IOException;
import java.time.LocalDateTime;
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
    private final QuestionRepository questionRepository;
    private final TopicContentRepository topicContentRepository;
    private final WalletService walletService;
    private final PaymentService paymentService;
    private final MPesaPaymentService mPesaPaymentService;
    private final EmolaPaymentService emolaPaymentService;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder, LoginAttemptService loginAttemptService, EmailService emailService, AmazonS3Service amazonS3Service, SubjectRepository subjectRepository, QuestionRepository questionRepository, TopicContentRepository topicContentRepository, WalletService walletService, PaymentService paymentService, MPesaPaymentService mPesaPaymentService, EmolaPaymentService emolaPaymentService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.loginAttemptService = loginAttemptService;
        this.emailService = emailService;
        this.amazonS3Service = amazonS3Service;
        this.subjectRepository = subjectRepository;
        this.questionRepository = questionRepository;
        this.topicContentRepository = topicContentRepository;
        this.walletService = walletService;
        this.paymentService = paymentService;
        this.mPesaPaymentService = mPesaPaymentService;
        this.emolaPaymentService = emolaPaymentService;
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
        user.setUserId(UUID.randomUUID().toString());
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
        //S3UploadResponse s3UploadResponse = amazonS3Service.uploadFile(profileImage, BUCKET_NAME);

        String fileKey = UUID.randomUUID() + "-" + profileImage.getOriginalFilename();
        S3UploadResponse s3UploadResponse = amazonS3Service.uploadFile(profileImage, BUCKET_NAME, fileKey);

        // Adicionar funcao que diminue o tamanho da imagem
        User user = new User();
        String password = generatePassword();
        user.setUserId(UUID.randomUUID().toString());
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
        user.setFileName(fileKey);
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
            //S3UploadResponse s3UploadResponse = amazonS3Service.uploadFile(profileImage, BUCKET_NAME);
            //currentUser.setProfileImageUrl(s3UploadResponse.getFileUrl());
            //currentUser.setFileName(profileImage.getOriginalFilename());

            String newFileKey = UUID.randomUUID() + "-" + profileImage.getOriginalFilename();
            S3UploadResponse s3UploadResponse = amazonS3Service.uploadFile(profileImage, BUCKET_NAME, newFileKey);
            currentUser.setProfileImageUrl(s3UploadResponse.getFileUrl());
            currentUser.setFileName(newFileKey);
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
            //S3UploadResponse s3UploadResponse = amazonS3Service.uploadFile(profileImage, BUCKET_NAME);
            //currentUser.setProfileImageUrl(s3UploadResponse.getFileUrl());
            //currentUser.setFileName(profileImage.getOriginalFilename());

            String newFileKey = UUID.randomUUID() + "-" + profileImage.getOriginalFilename();
            S3UploadResponse s3UploadResponse = amazonS3Service.uploadFile(profileImage, BUCKET_NAME, newFileKey);
            currentUser.setProfileImageUrl(s3UploadResponse.getFileUrl());
            currentUser.setFileName(newFileKey);
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

        String newFileKey = UUID.randomUUID() + "-" + profileImage.getOriginalFilename();
        S3UploadResponse s3UploadResponse = amazonS3Service.uploadFile(profileImage, BUCKET_NAME, newFileKey);
        currentUser.setProfileImageUrl(s3UploadResponse.getFileUrl());
        currentUser.setFileName(newFileKey);

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

        //S3UploadResponse s3UploadResponse = amazonS3Service.uploadFile(coverImage, BUCKET_NAME);
        //currentUser.setProfileCoverImageUrl(s3UploadResponse.getFileUrl());
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
    public User activatePlan(Long userId, Plan plan, Long walletId)
            throws UserNotFoundException, WalletNotFoundException {

        final int DAYS_VALID = 30;
        final double PLAN_PRICE = 299.0;

        // Buscar utilizador e carteira
        User user = findById(userId);
        Wallet wallet = walletService.findById(walletId);

        if (wallet == null) {
            throw new WalletNotFoundException("Carteira não encontrada.");
        }

        // Determinar tipo de carteira e simular pagamento
        boolean paymentSuccess;
        switch (wallet.getType()) {
            case MPESA:
                paymentSuccess = mPesaPaymentService.simulateMpesaPayment(wallet.getPhoneNumber(), plan);
                break;
            case EMOLA:
                paymentSuccess = emolaPaymentService.simulateEmolaPayment(wallet.getPhoneNumber(), plan);
                break;
            default:
                throw new IllegalArgumentException("Tipo de carteira inválido: " + wallet.getType());
        }

        if (!paymentSuccess) {
            throw new RuntimeException("Falha ao processar o pagamento. Tente novamente.");
        }

        // Atualizar plano e validade
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusDays(DAYS_VALID);
        user.setPlan(plan);
        user.setExpiresAt(expiresAt);

        // Registrar pagamento
        Payment payment = new Payment();
        payment.setUser(user);
        payment.setWallet(wallet);
        payment.setPlan(plan);
        payment.setAmount(PLAN_PRICE);
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setCreatedAt(now);
        payment.setExpiresAt(expiresAt);
        payment.setTransactionReference(UUID.randomUUID().toString()); // id de simulação

        paymentService.save(payment);

        // Salvar usuário com novo plano
        return userRepository.save(user);
    }

    @Override
    public boolean isPlanActive(User user, Plan plan) {
        return user.getPlan() == plan &&
                user.getExpiresAt() != null &&
                LocalDateTime.now().isBefore(user.getExpiresAt());
    }

    @Override
    public void cancelSubscription(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setPlan(Plan.FREE);
        user.setExpiresAt(null);
        userRepository.save(user);
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
    public User toggleTopicContentMarkedStatus(Long userId, Long topicContentId) throws UserNotFoundException, ContentNotFoundException {
        User user = findById(userId);

        TopicContent topicContent = topicContentRepository.findById(topicContentId)
                .orElseThrow(() -> new ContentNotFoundException("No Course Content found by id: " + topicContentId));

        Set<TopicContent> markedContents = user.getMarkedTopicContents();

        if (markedContents.contains(topicContent)) {
            markedContents.remove(topicContent);
        } else {
            markedContents.add(topicContent);
        }

        return userRepository.save(user);
    }

    @Override
    @Transactional
    public Question toggleSaveQuestion(Long userId, Long questionId) throws QuestionNotFoundException {
        User currentUser = userRepository.findById(userId).orElseThrow(null);

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new QuestionNotFoundException("Question not find"));

        Set<Question> savedQuestions = currentUser.getSavedQuestions();

        if (savedQuestions.contains(question)) {
            savedQuestions.remove(question);
        } else {
            savedQuestions.add(question);
        }

        userRepository.save(currentUser); // atualiza a relação

        return question;
    }

    @Override
    public boolean checkIfSavedQuestion(Long questionId, Long currentUserId) {
        User currentUser = userRepository.findById(currentUserId).orElse(null);
        Question question = questionRepository.findById(questionId).orElse(null);

        if (currentUser == null || question == null) {
            return false;
        }

        return currentUser.getSavedQuestions().contains(question);
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

    @Override
    public List<User> getUsers() {
        return userRepository.findAll();
    }

}
