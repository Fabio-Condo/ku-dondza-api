package com.fabiocondo.controller;


import com.fabiocondo.domain.*;
import com.fabiocondo.enumeration.UserType;
import com.fabiocondo.exception.domain.*;
import com.fabiocondo.repository.filter.UserFilter;
import com.fabiocondo.service.impl.AuthServiceImpl;
import com.fabiocondo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.MessagingException;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import static com.fabiocondo.constant.UserImplConstant.EMAIL_SENT;
import static com.fabiocondo.constant.UserImplConstant.USER_DELETED_SUCCESSFULLY;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping(path = { "/", "/user"})
public class UserController {
    private final AuthenticationManager authenticationManager;
    private final UserService userService;

    @Autowired
    public UserController(AuthenticationManager authenticationManager, UserService userService, AuthServiceImpl authServiceImpl) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
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
                                           @RequestParam(value = "profileImage", required = false) MultipartFile profileImage) throws UserNotFoundException, EmailExistException, MessagingException {
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

    @GetMapping("/{userId}/subscribedOnlineCourses")
    public Page<Course> getSubscribedOnlineCoursesByUserId(@PathVariable Long userId, Pageable pageable) throws UserNotFoundException {
        return userService.getSubscribedOnlineCoursesByUserId(userId, pageable);
    }

    @GetMapping("/{userId}/subscribedOnlineCourses/total")
    public ResponseEntity<Long> countSubscribedOnlineCoursesByUserId(@PathVariable Long userId){
        return ResponseEntity.status(HttpStatus.OK).body(userService.countSubscribedOnlineCoursesByUserId(userId));
    }

    @PostMapping("/{userId}/subscribedOnlineCourses/{onlineCourseId}")
    public ResponseEntity<User> toggleCourseSubscription(@PathVariable Long userId, @PathVariable Long onlineCourseId) throws UserNotFoundException, OnlineCourseNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(userService.toggleCourseSubscription(userId, onlineCourseId));
    }

    @PutMapping("/{userId}/marked-contents/{contentId}/toggle")
    public ResponseEntity<User> toggleMarkedContent(@PathVariable Long userId, @PathVariable Long contentId) throws UserNotFoundException, CourseContentNotFoundException {
        return ResponseEntity.status(OK).body(userService.toggleContentMarkedStatus(userId, contentId));
    }

    private ResponseEntity<HttpResponse> response(HttpStatus httpStatus, String message) {
        return new ResponseEntity<>(new HttpResponse(httpStatus.value(), httpStatus, httpStatus.getReasonPhrase().toUpperCase(),
                message), httpStatus);
    }
}
