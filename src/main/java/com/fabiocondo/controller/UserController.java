package com.fabiocondo.controller;


import com.fabiocondo.domain.*;
import com.fabiocondo.exception.domain.*;
import com.fabiocondo.security.utility.JWTTokenProvider;
import com.fabiocondo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.List;
import java.util.Set;

import static com.fabiocondo.constant.SecurityConstant.JWT_TOKEN_HEADER;
import static com.fabiocondo.constant.UserImplConstant.EMAIL_SENT;
import static com.fabiocondo.constant.UserImplConstant.USER_DELETED_SUCCESSFULLY;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping(path = { "/", "/user"})
public class UserController {
    private AuthenticationManager authenticationManager;
    private UserService userService;
    private JWTTokenProvider jwtTokenProvider;

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

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody User user) throws UserNotFoundException, UsernameExistException, EmailExistException, MessagingException {
        User newUser = userService.register(user.getFirstName(), user.getLastName(), user.getUsername(), user.getEmail());
        return ResponseEntity.status(HttpStatus.OK).body(newUser);
    }

    @PostMapping("/add")
    public ResponseEntity<User> addNewUser(@RequestParam("firstName") String firstName,
                                           @RequestParam("lastName") String lastName,
                                           @RequestParam("username") String username,
                                           @RequestParam("email") String email,
                                           @RequestParam("role") String role,
                                           @RequestParam("isActive") String isActive,
                                           @RequestParam("isNonLocked") String isNonLocked,
                                           @RequestParam(value = "profileImage", required = false) MultipartFile profileImage) throws UserNotFoundException, UsernameExistException, EmailExistException, MessagingException {
        User newUser = userService.addNewUser(firstName, lastName, username,email, role, Boolean.parseBoolean(isNonLocked), Boolean.parseBoolean(isActive), profileImage);
        return ResponseEntity.status(HttpStatus.OK).body(newUser);
    }

    @PutMapping("/update")
    public ResponseEntity<User> update(@RequestParam("currentUsername") String currentUsername,
                                       @RequestParam("firstName") String firstName,
                                       @RequestParam("lastName") String lastName,
                                       @RequestParam("username") String username,
                                       @RequestParam("email") String email,
                                       @RequestParam("role") String role,
                                       @RequestParam("isActive") String isActive,
                                       @RequestParam("isNonLocked") String isNonLocked,
                                       @RequestParam(value = "profileImage", required = false) MultipartFile profileImage) throws UserNotFoundException, UsernameExistException, EmailExistException, IOException, NotAnImageFileException {
        User updatedUser = userService.updateUser(currentUsername, firstName, lastName, username,email, role, Boolean.parseBoolean(isNonLocked), Boolean.parseBoolean(isActive), profileImage);
        return ResponseEntity.status(HttpStatus.OK).body(updatedUser);
    }

    @PutMapping("/update-user-profile")
    public ResponseEntity<User> updateUserProfile(@RequestParam("currentUsername") String currentUsername,
                                       @RequestParam("firstName") String firstName,
                                       @RequestParam("lastName") String lastName,
                                       @RequestParam("username") String username,
                                       @RequestParam("email") String email,
                                       @RequestParam("institution") String institution,
                                       @RequestParam("bio") String bio,
                                       @RequestParam("course") String course,
                                       @RequestParam("role") String role,
                                       @RequestParam("isActive") String isActive,
                                       @RequestParam("isNonLocked") String isNonLocked,
                                       @RequestParam(value = "profileImage", required = false) MultipartFile profileImage) throws UserNotFoundException, UsernameExistException, EmailExistException, IOException, NotAnImageFileException {
        User updatedUser = userService.updateUserProfile(currentUsername, firstName, lastName, username,email, institution, bio, course, role, Boolean.parseBoolean(isNonLocked), Boolean.parseBoolean(isActive), profileImage);
        return ResponseEntity.status(HttpStatus.OK).body(updatedUser);
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> update(@PathVariable("id") Long id, @RequestBody User user) throws CourseNotFoundException {
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
    public Page<User> findAll(@RequestParam(required = false, defaultValue = "") String name, Pageable pageable) throws UserNotFoundException {
        return userService.findAll(name, pageable);
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

    @PostMapping("/{userId}/interests/{interestId}")
    public ResponseEntity<User> addInterestToUserInterests(@PathVariable Long userId, @PathVariable Long interestId) throws InterestNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(userService.addInterestToUserInterests(userId, interestId));
    }

    @DeleteMapping("/{userId}/interests/{interestId}")
    public ResponseEntity<User> removeInterestFromUserInterests(@PathVariable Long userId, @PathVariable Long interestId) throws InterestNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(userService.removeInterestFromUserInterests(userId, interestId));
    }

    @PostMapping("/{userId}/savedPosts/{postId}")
    public ResponseEntity<User> addPostToSavedPosts(@PathVariable Long userId, @PathVariable Long postId) throws PostNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(userService.addPostToSavedPosts(userId, postId));
    }

    @DeleteMapping("/{userId}/savedPosts/{postId}")
    public ResponseEntity<User> removePostFromSavedPosts(@PathVariable Long userId, @PathVariable Long postId) throws PostNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(userService.removePostFromSavedPosts(userId, postId));
    }

    @GetMapping("/{userId}/savedPosts/list")
    public ResponseEntity<List<Post>> getSavedPosts(@PathVariable Long userId) throws UserNotFoundException {
        List<Post> savedPosts = userService.getSavedPosts(userId);
        return ResponseEntity.status(HttpStatus.OK).body(savedPosts);
    }

    @GetMapping("/{userId}/savedPosts")
    public Page<Post> findSavedPostsByUserId(@PathVariable Long userId, Pageable pageable) throws UserNotFoundException {
        return userService.findSavedPostsByUserId(userId, pageable);
    }

    @GetMapping("/{userId}/savedPosts/count")
    public long countSavedPosts(@PathVariable Long userId) throws UserNotFoundException {
        return userService.countSavedPostsByUser(userId);
    }

    @GetMapping("/{userId}/savedPosts/contains/{postId}")
    public ResponseEntity<Boolean> doesUserSavedPost(@PathVariable Long userId, @PathVariable Long postId) {
        boolean doesContain = userService.doesUserSavedPost(userId, postId);
        return ResponseEntity.status(HttpStatus.OK).body(doesContain);
    }

    @GetMapping("/friend-requests")
    public ResponseEntity<Set<User>> getFriendRequests() throws UserNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(userService.getFriendRequests());
    }

    @PostMapping("/send-friend-request")
    public void sendFriendRequest(@RequestBody User friend) throws UserNotFoundException {
        userService.sendFriendRequest(friend);
    }

    @PostMapping("/accept-friend-requests/{friendId}")
    public User acceptFriendRequest(@PathVariable Long friendId) throws UserNotFoundException {
        return userService.acceptFriendRequest(friendId);
    }

    @DeleteMapping("/reject-friend-requests/{friendId}")
    public void rejectFriendRequest(@PathVariable Long friendId) throws UserNotFoundException {
        userService.rejectFriendRequest(friendId);
    }

    @GetMapping("/friends")
    public Set<User> getFriends() throws UserNotFoundException {
        return userService.getFriends();
    }

    @GetMapping("/{userId}/friends")
    public Page<User> getFriendsById(@PathVariable Long userId, Pageable pageable) throws UserNotFoundException {
        return userService.getFriends(userId, pageable);
    }

    @DeleteMapping("/friends/{friendId}")
    public void removeFriend(@PathVariable Long friendId) throws UserNotFoundException {
        userService.removeFriend(friendId);
    }

    @PostMapping("/{userId}/subscribedOnlineCourses/{onlineCourseId}")
    public ResponseEntity<User> addCourseToSubscribedOnlineCourses(@PathVariable Long userId, @PathVariable Long onlineCourseId) throws PostNotFoundException, CourseNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(userService.addCourseToSubscribedOnlineCourses(userId, onlineCourseId));
    }

    @DeleteMapping("/{userId}/subscribedOnlineCourses/{onlineCourseId}")
    public ResponseEntity<User> removeCourseFromSubscribedOnlineCourses(@PathVariable Long userId, @PathVariable Long onlineCourseId) throws PostNotFoundException, CourseNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(userService.removeCourseFromSubscribedOnlineCourses(userId, onlineCourseId));
    }

    @GetMapping("/{userId}/subscribedOnlineCourses/contains/{onlineCourseId}")
    public ResponseEntity<Boolean> doesUserSubscribedOnlineCourse(@PathVariable Long userId, @PathVariable Long onlineCourseId) {
        boolean doesContain = userService.doesUserSubscribedOnlineCourse(userId, onlineCourseId);
        return ResponseEntity.status(HttpStatus.OK).body(doesContain);
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
