package com.fabiocondo.controller;


import com.fabiocondo.domain.*;
import com.fabiocondo.enumeration.UserType;
import com.fabiocondo.exception.domain.*;
import com.fabiocondo.security.utility.JWTTokenProvider;
import com.fabiocondo.service.UserService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.MessagingException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.fabiocondo.constant.SecurityConstant.JWT_TOKEN_HEADER;
import static com.fabiocondo.constant.UserImplConstant.EMAIL_SENT;
import static com.fabiocondo.constant.UserImplConstant.USER_DELETED_SUCCESSFULLY;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping(path = { "/", "/user"})
public class UserController {
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JWTTokenProvider jwtTokenProvider;
    private static final String CLIENT_ID = "170476897572-k758vjru9e2qqa707qhb5ns2kaaegquc.apps.googleusercontent.com";
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    public UserController(AuthenticationManager authenticationManager, UserService userService, JWTTokenProvider jwtTokenProvider) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/login")
    public ResponseEntity<User> login(@RequestBody User user) {
        authenticate(user.getUsername(), user.getPassword());
        User loginUser = userService.findUserByUsername(user.getUsername());
        UserPrincipal userPrincipal = new UserPrincipal(loginUser);
        HttpHeaders jwtHeader = getJwtHeader(userPrincipal);
        return new ResponseEntity<>(loginUser, jwtHeader, OK);
    }

    @PostMapping("/auth/google")
    public ResponseEntity<?> processGoogleLogin(@RequestBody String idTokenString) {
        logger.info("Token recebido: " + idTokenString);

        try {

            JsonFactory jsonFactory = GsonFactory.getDefaultInstance();  // Usando GsonFactory
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), jsonFactory)
                    .setAudience(Collections.singletonList(CLIENT_ID))
                    .build();

            GoogleIdToken idToken = verifier.verify(extractIdToken(idTokenString));
            if (idToken != null) {
                GoogleIdToken.Payload payload = idToken.getPayload();

                // Print user identifier
                String userId = payload.getSubject();
                logger.info("User ID: " + userId);

                String email = payload.getEmail();
                boolean emailVerified = Boolean.valueOf(payload.getEmailVerified());
                String name = (String) payload.get("name");
                String pictureUrl = (String) payload.get("picture");
                String locale = (String) payload.get("locale");
                String familyName = (String) payload.get("family_name");
                String givenName = (String) payload.get("given_name");

                // Buscar usuário pelo email
                User loginUser = userService.findUserByEmail(email);

                // Caso o usuário não exista, crie um novo
                if (loginUser == null) {
                    loginUser = userService.register(name,  "", email, pictureUrl);
                }

                // Gerar token JWT para o usuário
                UserPrincipal userPrincipal = new UserPrincipal(loginUser);
                HttpHeaders jwtHeader = getJwtHeader(userPrincipal);

                return new ResponseEntity<>(loginUser, jwtHeader, OK);
            } else {
                return ResponseEntity.badRequest().body(null);  // Token inválido
            }
        } catch (GeneralSecurityException | IOException e) {
            logger.error("Erro ao verificar o token: ", e);
            return ResponseEntity.internalServerError().body(null);  // Erro ao verificar o token
        } catch (JSONException | EmailExistException | UserNotFoundException | MessagingException |
                 UsernameExistException e) {
            throw new RuntimeException(e);
        }
    }

    public static String extractIdToken(String jsonString) throws JSONException {
        // Converte a string JSON em um objeto JSONObject
        JSONObject jsonObject = new JSONObject(jsonString);

        // Extrai o valor do idToken
        return jsonObject.getString("idToken");
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody User user) throws UserNotFoundException, UsernameExistException, EmailExistException, MessagingException {
        User newUser = userService.register(user.getFullName(), user.getUsername(), user.getEmail(), null);
        return ResponseEntity.status(HttpStatus.OK).body(newUser);
    }

    @PostMapping("/add")
    public ResponseEntity<User> addNewUser(@RequestParam("fullName") String fullName,
                                           @RequestParam("username") String username,
                                           @RequestParam("email") String email,
                                           @RequestParam("userType") UserType userType,
                                           @RequestParam("role") String role,
                                           @RequestParam("isActive") String isActive,
                                           @RequestParam("isNonLocked") String isNonLocked,
                                           @RequestParam(value = "profileImage", required = false) MultipartFile profileImage) throws UserNotFoundException, UsernameExistException, EmailExistException, MessagingException {
        User newUser = userService.addNewUser(fullName, username,email, role, userType, Boolean.parseBoolean(isNonLocked), Boolean.parseBoolean(isActive), profileImage);
        return ResponseEntity.status(HttpStatus.OK).body(newUser);
    }

    @PutMapping("/update")
    public ResponseEntity<User> update(@RequestParam("currentUsername") String currentUsername,
                                       @RequestParam("fullName") String fullName,
                                       @RequestParam("username") String username,
                                       @RequestParam("email") String email,
                                       @RequestParam("userType") UserType userType,
                                       @RequestParam("role") String role,
                                       @RequestParam("isActive") String isActive,
                                       @RequestParam("isNonLocked") String isNonLocked,
                                       @RequestParam(value = "profileImage", required = false) MultipartFile profileImage) throws UserNotFoundException, UsernameExistException, EmailExistException, IOException, NotAnImageFileException {
        User updatedUser = userService.updateUser(currentUsername, fullName, username,email, role, userType, Boolean.parseBoolean(isNonLocked), Boolean.parseBoolean(isActive), profileImage);
        return ResponseEntity.status(HttpStatus.OK).body(updatedUser);
    }

    @PutMapping("/update-user-profile")
    public ResponseEntity<User> updateUserProfile(@RequestParam("currentUsername") String currentUsername,
                                       @RequestParam("fullName") String fullName,
                                       @RequestParam("username") String username,
                                       @RequestParam("email") String email,
                                       @RequestParam("bio") String bio,
                                       @RequestParam("role") String role,
                                       @RequestParam("isActive") String isActive,
                                       @RequestParam("isNonLocked") String isNonLocked,
                                       @RequestParam(value = "profileImage", required = false) MultipartFile profileImage) throws UserNotFoundException, UsernameExistException, EmailExistException, IOException, NotAnImageFileException {
        User updatedUser = userService.updateUserProfile(currentUsername, fullName, username,email, bio, role, Boolean.parseBoolean(isNonLocked), Boolean.parseBoolean(isActive), profileImage);
        return ResponseEntity.status(HttpStatus.OK).body(updatedUser);
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> update(@PathVariable("id") Long id, @RequestBody User user) throws UserNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(userService.update(user, id));
    }

    @PostMapping("/{userId}/profile-photo")
    public ResponseEntity<User> updateProfilePhoto(@PathVariable String userId, @RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity.status(HttpStatus.OK).body(userService.updateUserProfilePhoto(userId, file));
    }

    @PostMapping("/{userId}/cover-photo")
    public ResponseEntity<User> updateUserProfileCoverPhoto(@PathVariable String userId, @RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity.status(HttpStatus.OK).body(userService.updateUserProfileCoverPhoto(userId, file));
    }
    
    @GetMapping("/find/{username}")
    public ResponseEntity<User> getUser(@PathVariable("username") String username) {
        User user = userService.findUserByUsername(username);
        return ResponseEntity.status(HttpStatus.OK).body(user);
    }

    @GetMapping("/find-by-user-id/{userId}")
    public ResponseEntity<User> getUserByUserId(@PathVariable("userId") String userId) {
        User user = userService.findUserByUserId(userId);
        return ResponseEntity.status(HttpStatus.OK).body(user);
    }

    @GetMapping("/list")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getUsers();
        return ResponseEntity.status(HttpStatus.OK).body(users);
    }

    @GetMapping("/list/pageable")
    public Page<User> findAll(@RequestParam(required = false, defaultValue = "") String searchParam, Pageable pageable) throws UserNotFoundException {
        return userService.findAll(searchParam, pageable);
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

    @DeleteMapping("/delete/{username}")
    @PreAuthorize("hasAnyAuthority('user:delete')")
    public ResponseEntity<HttpResponse> deleteUser(@PathVariable("username") String username) throws IOException {
        userService.deleteUser(username);
        return response(OK, USER_DELETED_SUCCESSFULLY);
    }

    @PostMapping("/updateProfileImage")
    public ResponseEntity<User> updateProfileImage(@RequestParam("username") String username, @RequestParam(value = "profileImage") MultipartFile profileImage) throws UserNotFoundException, UsernameExistException, EmailExistException, IOException {
        User user = userService.updateProfileImage(username, profileImage);
        return ResponseEntity.status(HttpStatus.OK).body(user);
    }

    @PutMapping("/{newUsername}/active-user")
    @PreAuthorize("hasAnyAuthority('user:update')")
    public void updatePropertyActive(@PathVariable("newUsername") String newUsername, @RequestBody Boolean active) throws UsernameNotFoundException {
        userService.updatePropertyActive(newUsername, active);
    }

    @PutMapping("/{newUsername}/notLocked-user")
    @PreAuthorize("hasAnyAuthority('user:update')")
    public void updatePropertyNotLocked(@PathVariable("newUsername") String newUsername, @RequestBody Boolean notLocked) throws UsernameNotFoundException {
        userService.updatePropertyNotLocked(newUsername, notLocked);
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

    @GetMapping("/{userId}/subscribedOnlineCourses")
    public Page<OnlineCourse> getSubscribedOnlineCoursesByUserId(@PathVariable Long userId, Pageable pageable) throws UserNotFoundException {
        return userService.getSubscribedOnlineCoursesByUserId(userId, pageable);
    }

    @GetMapping("/{userId}/subscribedOnlineCourses/total")
    public ResponseEntity<Long> countSubscribedOnlineCoursesByUserId(@PathVariable Long userId){
        return ResponseEntity.status(HttpStatus.OK).body(userService.countSubscribedOnlineCoursesByUserId(userId));
    }

    @PostMapping("/{userId}/subscribedOnlineCourses/{onlineCourseId}")
    public ResponseEntity<User> addCourseToSubscribedOnlineCourses(@PathVariable Long userId, @PathVariable Long onlineCourseId) throws UserNotFoundException, OnlineCourseNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(userService.addCourseToSubscribedOnlineCourses(userId, onlineCourseId));
    }

    @DeleteMapping("/{userId}/subscribedOnlineCourses/{onlineCourseId}")
    public ResponseEntity<User> removeCourseFromSubscribedOnlineCourses(@PathVariable Long userId, @PathVariable Long onlineCourseId) throws UserNotFoundException, OnlineCourseNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(userService.removeCourseFromSubscribedOnlineCourses(userId, onlineCourseId));
    }

    @PostMapping("/{userId}/marked-course-content/{onlineCourseContentId}")
    public ResponseEntity<User> addContentToMarkedCourseContents(@PathVariable Long userId, @PathVariable Long onlineCourseContentId) throws UserNotFoundException, CourseContentNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(userService.addContentToMarkedCourseContents(userId, onlineCourseContentId));
    }

    @DeleteMapping("/{userId}/marked-course-content/{onlineCourseContentId}")
    public ResponseEntity<User> removeContentFromMarkedCourseContents(@PathVariable Long userId, @PathVariable Long onlineCourseContentId) throws UserNotFoundException, CourseContentNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(userService.removeContentFromMarkedCourseContents(userId, onlineCourseContentId));
    }

    @GetMapping("/{userId}/marked-course-content/contains/{onlineCourseContentId}")
    public ResponseEntity<Boolean> checkIfMarkedCourseContent(@PathVariable Long userId, @PathVariable Long onlineCourseContentId) {
        boolean markedCourseContent = userService.checkIfMarkedCourseContent(userId, onlineCourseContentId);
        return ResponseEntity.ok(markedCourseContent);
    }

    private ResponseEntity<HttpResponse> response(HttpStatus httpStatus, String message) {
        return new ResponseEntity<>(new HttpResponse(httpStatus.value(), httpStatus, httpStatus.getReasonPhrase().toUpperCase(),
                message), httpStatus);
    }

    private HttpHeaders getJwtHeader(UserPrincipal user) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(JWT_TOKEN_HEADER, jwtTokenProvider.generateJwtToken(user));
        return headers;
    }

    private void authenticate(String username, String password) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
    }
}
