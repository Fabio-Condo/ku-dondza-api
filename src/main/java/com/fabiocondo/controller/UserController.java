package com.fabiocondo.controller;


import com.fabiocondo.constant.CacheNames;
import com.fabiocondo.domain.*;
import com.fabiocondo.dto.UserDTO;
import com.fabiocondo.dtoMapper.UserMapper;
import com.fabiocondo.enumeration.Plan;
import com.fabiocondo.enumeration.UserType;
import com.fabiocondo.exception.domain.*;
import com.fabiocondo.repository.TopicContentRepository;
import com.fabiocondo.repository.filter.UserFilter;
import com.fabiocondo.security.utility.JWTTokenProvider;
import com.fabiocondo.service.impl.AuthServiceImpl;
import com.fabiocondo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.MessagingException;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import static com.fabiocondo.constant.SecurityConstant.JWT_TOKEN_HEADER;
import static com.fabiocondo.constant.UserImplConstant.EMAIL_SENT;
import static com.fabiocondo.constant.UserImplConstant.USER_DELETED_SUCCESSFULLY;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping(path = { "/", "/user"})
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;
    private final TopicContentRepository topicContentRepository;
    private final JWTTokenProvider jwtTokenProvider;

    @Autowired
    public UserController(UserService userService, AuthServiceImpl authServiceImpl, UserMapper userMapper, TopicContentRepository topicContentRepository, JWTTokenProvider jwtTokenProvider) {
        this.userService = userService;
        this.userMapper = userMapper;
        this.topicContentRepository = topicContentRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody User user) throws UserNotFoundException, EmailExistException, MessagingException {
        User newUser = userService.register(user.getFullName(), user.getEmail(), null);
        return ResponseEntity.status(HttpStatus.OK).body(newUser);
    }

    @PostMapping("/add")
    public ResponseEntity<User> addNewUser(@RequestParam("fullName") String fullName,
                                           @RequestParam("email") String email,
                                           @RequestParam("userType") UserType userType,
                                           @RequestParam("role") String role,
                                           @RequestParam("isActive") String isActive,
                                           @RequestParam("isNonLocked") String isNonLocked,
                                           @RequestParam(value = "profileImage", required = false) MultipartFile profileImage) throws UserNotFoundException, EmailExistException, MessagingException, IOException {
        User newUser = userService.addNewUser(fullName,email, role, userType, Boolean.parseBoolean(isNonLocked), Boolean.parseBoolean(isActive), profileImage);
        return ResponseEntity.status(HttpStatus.OK).body(newUser);
    }

    @PutMapping("/update")
    public ResponseEntity<User> update(@RequestParam("currentEmail") String currentEmail,
                                       @RequestParam("fullName") String fullName,
                                       @RequestParam("email") String email,
                                       @RequestParam("userType") UserType userType,
                                       @RequestParam("role") String role,
                                       @RequestParam("isActive") String isActive,
                                       @RequestParam("isNonLocked") String isNonLocked,
                                       @RequestParam(value = "profileImage", required = false) MultipartFile profileImage) throws UserNotFoundException, EmailExistException, IOException, NotAnImageFileException {
        User updatedUser = userService.updateUser(currentEmail, fullName, email, role, userType, Boolean.parseBoolean(isNonLocked), Boolean.parseBoolean(isActive), profileImage);
        return ResponseEntity.status(HttpStatus.OK).body(updatedUser);
    }

    @PutMapping("/update-user-profile")
    public ResponseEntity<User> updateUserProfile(@RequestParam("currentEmail") String currentEmail,
                                       @RequestParam("fullName") String fullName,
                                       @RequestParam("email") String email,
                                       @RequestParam("bio") String bio,
                                       @RequestParam("role") String role,
                                       @RequestParam("isActive") String isActive,
                                       @RequestParam("isNonLocked") String isNonLocked,
                                       @RequestParam(value = "profileImage", required = false) MultipartFile profileImage) throws UserNotFoundException, EmailExistException, IOException, NotAnImageFileException {
        User updatedUser = userService.updateUserProfile(currentEmail, fullName, email, bio, role, Boolean.parseBoolean(isNonLocked), Boolean.parseBoolean(isActive), profileImage);
        return ResponseEntity.status(HttpStatus.OK).body(updatedUser);
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> update(@PathVariable("id") Long id, @RequestBody User user) throws UserNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(userService.update(user, id));
    }

    @PostMapping("/{userId}/profile-photo")
    public ResponseEntity<User> updateProfilePhoto(@PathVariable String userId, @RequestParam("file") MultipartFile file) throws IOException, EmailNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(userService.updateUserProfilePhoto(userId, file));
    }

    @PostMapping("/{userId}/cover-photo")
    public ResponseEntity<User> updateUserProfileCoverPhoto(@PathVariable String userId, @RequestParam("file") MultipartFile file) throws IOException, EmailNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(userService.updateUserProfileCoverPhoto(userId, file));
    }
    
    @GetMapping("/find/{email}")
    public ResponseEntity<User> getUser(@PathVariable("email") String email) {
        User user = userService.findUserByEmail(email);
        return ResponseEntity.status(HttpStatus.OK).body(user);
    }

    @GetMapping("/find-by-user-id/{userId}")
    public ResponseEntity<User> getUserByUserId(@PathVariable("userId") String userId) throws UserNotFoundException {
        User user = userService.findUserByUserId(userId);
        return ResponseEntity.status(HttpStatus.OK).body(user);
    }

    @GetMapping("/list")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getUsers();
        return ResponseEntity.status(HttpStatus.OK).body(users);
    }

    @GetMapping("/filter")
    public Page<User> filter(UserFilter userFilter, Pageable pageable) {
        return userService.filter(userFilter, pageable);
    }

    @GetMapping("/instrutores")
    public List<User> getAllInstrutores() {
        return userService.getAllInstrutores();
    }

    @GetMapping("/total")
    public ResponseEntity<Long> getTotal(){
        return ResponseEntity.status(HttpStatus.OK).body(userService.getTotal());
    }

    @GetMapping("/resetpassword/{email}")
    public ResponseEntity<HttpResponse> resetPassword(@PathVariable("email") String email) throws MessagingException, EmailNotFoundException {
        userService.resetPassword(email);
        return response(OK, EMAIL_SENT + email);
    }

    @DeleteMapping("/delete/{email}")
    @PreAuthorize("hasAnyAuthority('user:delete')")
    public ResponseEntity<HttpResponse> deleteUser(@PathVariable("email") String email) throws IOException {
        userService.deleteUser(email);
        return response(OK, USER_DELETED_SUCCESSFULLY);
    }

    @PostMapping("/updateProfileImage")
    public ResponseEntity<User> updateProfileImage(@RequestParam("email") String email, @RequestParam(value = "profileImage") MultipartFile profileImage) throws UserNotFoundException, EmailExistException, IOException {
        User user = userService.updateProfileImage(email, profileImage);
        return ResponseEntity.status(HttpStatus.OK).body(user);
    }

    @PutMapping("/{newEmail}/active-user")
    @PreAuthorize("hasAnyAuthority('user:update')")
    public void updatePropertyActive(@PathVariable("newEmail") String newEmail, @RequestBody Boolean active) throws EmailNotFoundException {
        userService.updatePropertyActive(newEmail, active);
    }

    @PutMapping("/activate-plan/{userId}")
    public ResponseEntity<User> activatePlan(
            @PathVariable Long userId,
            @RequestParam Plan plan,
            @RequestParam Long walletId) throws UserNotFoundException, WalletNotFoundException, PaymentException, MessagingException {

        // Ativar plano com a carteira selecionada
        User user = userService.activatePlan(userId, plan, walletId);

        // 🔹 Gerar JWT e cabeçalhos
        UserPrincipal userPrincipal = new UserPrincipal(user);
        HttpHeaders jwtHeader = getJwtHeader(userPrincipal);

        return new ResponseEntity<>(user, jwtHeader, HttpStatus.OK);
    }

    private HttpHeaders getJwtHeader(UserPrincipal user) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(JWT_TOKEN_HEADER, jwtTokenProvider.generateJwtToken(user));
        return headers;
    }

    @PutMapping("/{newEmail}/notLocked-user")
    @PreAuthorize("hasAnyAuthority('user:update')")
    public void updatePropertyNotLocked(@PathVariable("newEmail") String newEmail, @RequestBody Boolean notLocked) throws EmailNotFoundException {
        userService.updatePropertyNotLocked(newEmail, notLocked);
    }

    @GetMapping("/{userId}/interests")
    public ResponseEntity<Set<Subject>> getUserSubjectInterests(@PathVariable Long userId) throws UserNotFoundException {
        Set<Subject> savedPosts = userService.getUserSubjectInterests(userId);
        return ResponseEntity.status(HttpStatus.OK).body(savedPosts);
    }

    @PostMapping("/{userId}/interests/{interestId}")
    public ResponseEntity<User> addInterestToUserInterests(@PathVariable Long userId, @PathVariable Long interestId) throws SubjectNotFoundException, UserNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(userService.addInterestToUserInterests(userId, interestId));
    }

    @DeleteMapping("/{userId}/interests/{interestId}")
    public ResponseEntity<User> removeInterestFromUserInterests(@PathVariable Long userId, @PathVariable Long interestId) throws SubjectNotFoundException, UserNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(userService.removeInterestFromUserInterests(userId, interestId));
    }

    //@PutMapping("/{userId}/marked-topic-contents/{contentId}/toggle")
    public ResponseEntity<User> toggleTopicContentMarkedStatus(@PathVariable Long userId, @PathVariable Long contentId) throws UserNotFoundException, ContentNotFoundException {
        return ResponseEntity.status(OK).body(userService.toggleTopicContentMarkedStatus(userId, contentId));
    }

    @CacheEvict(
            value = {
                    CacheNames.SUBJECT_LIST,
                    CacheNames.SUBJECT_FILTER,
                    CacheNames.SUBJECT_DETAIL,
            },
            allEntries = true
    )
    @PutMapping("/{userId}/marked-topic-contents/{contentId}/toggle")
    public ResponseEntity<UserDTO> toggleMarkedContent(
            @PathVariable Long userId,
            @PathVariable Long contentId
    ) throws UserNotFoundException, ContentNotFoundException {

        // Busca o conteúdo
        TopicContent content = topicContentRepository.findById(contentId)
                .orElseThrow(() -> new ContentNotFoundException("Content not found with id: " + contentId));

        // Alterna o status de marcado/desmarcado
        User user = userService.toggleTopicContentMarkedStatus(userId, contentId);

        // Mapeia o usuário com a taxa de progresso atualizada
        Long subjectId = content.getTopic().getSubject().getId();
        UserDTO userDTO = userMapper.domainToDtoWithMarkedContentRate(subjectId, user);

        System.out.println("Rate: " + userDTO.getMarkedContentRate());

        // Retorna a resposta
        return ResponseEntity.ok(userDTO);
    }

    @PutMapping("/{userId}/saved-questions/{questionId}/toggle")
    public ResponseEntity<Question> toggleSaveQuestion(@PathVariable Long userId, @PathVariable Long questionId) throws QuestionNotFoundException {
        return ResponseEntity.status(OK).body(userService.toggleSaveQuestion(userId, questionId));
    }

    //@GetMapping("/filter")
    //public Page<User> filter(UserFilter userFilter, Pageable pageable) {
    //    return userService.filter(userFilter, pageable);
    //}

    private ResponseEntity<HttpResponse> response(HttpStatus httpStatus, String message) {
        return new ResponseEntity<>(new HttpResponse(httpStatus.value(), httpStatus, httpStatus.getReasonPhrase().toUpperCase(),
                message), httpStatus);
    }
}
