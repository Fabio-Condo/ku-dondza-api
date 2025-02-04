package com.fabiocondo.controller;


import com.fabiocondo.domain.*;
import com.fabiocondo.enumeration.ExamType;
import com.fabiocondo.enumeration.UserType;
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
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JWTTokenProvider jwtTokenProvider;

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
                                           @RequestParam("userType") UserType userType,
                                           @RequestParam("role") String role,
                                           @RequestParam("isActive") String isActive,
                                           @RequestParam("isNonLocked") String isNonLocked,
                                           @RequestParam(value = "profileImage", required = false) MultipartFile profileImage) throws UserNotFoundException, UsernameExistException, EmailExistException, MessagingException {
        User newUser = userService.addNewUser(firstName, lastName, username,email, role, userType, Boolean.parseBoolean(isNonLocked), Boolean.parseBoolean(isActive), profileImage);
        return ResponseEntity.status(HttpStatus.OK).body(newUser);
    }

    @PutMapping("/update")
    public ResponseEntity<User> update(@RequestParam("currentUsername") String currentUsername,
                                       @RequestParam("firstName") String firstName,
                                       @RequestParam("lastName") String lastName,
                                       @RequestParam("username") String username,
                                       @RequestParam("email") String email,
                                       @RequestParam("userType") UserType userType,
                                       @RequestParam("role") String role,
                                       @RequestParam("isActive") String isActive,
                                       @RequestParam("isNonLocked") String isNonLocked,
                                       @RequestParam(value = "profileImage", required = false) MultipartFile profileImage) throws UserNotFoundException, UsernameExistException, EmailExistException, IOException, NotAnImageFileException {
        User updatedUser = userService.updateUser(currentUsername, firstName, lastName, username,email, role, userType, Boolean.parseBoolean(isNonLocked), Boolean.parseBoolean(isActive), profileImage);
        return ResponseEntity.status(HttpStatus.OK).body(updatedUser);
    }

    @PutMapping("/update-user-profile")
    public ResponseEntity<User> updateUserProfile(@RequestParam("currentUsername") String currentUsername,
                                       @RequestParam("firstName") String firstName,
                                       @RequestParam("lastName") String lastName,
                                       @RequestParam("username") String username,
                                       @RequestParam("email") String email,
                                       @RequestParam("bio") String bio,
                                       @RequestParam("role") String role,
                                       @RequestParam("isActive") String isActive,
                                       @RequestParam("isNonLocked") String isNonLocked,
                                       @RequestParam(value = "profileImage", required = false) MultipartFile profileImage) throws UserNotFoundException, UsernameExistException, EmailExistException, IOException, NotAnImageFileException {
        User updatedUser = userService.updateUserProfile(currentUsername, firstName, lastName, username,email, bio, role, Boolean.parseBoolean(isNonLocked), Boolean.parseBoolean(isActive), profileImage);
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

    @GetMapping("/{userId}/savedBlogs/list")
    public ResponseEntity<Set<Blog>> getSavedBlogs(@PathVariable Long userId) throws UserNotFoundException {
        Set<Blog> savedBlogs = userService.getSavedBlogs(userId);
        return ResponseEntity.status(HttpStatus.OK).body(savedBlogs);
    }

    @PostMapping("/{userId}/savedBlogs/{blogId}")
    public ResponseEntity<User> addBlogToSavedBlogPosts(@PathVariable Long userId, @PathVariable Long blogId) throws UserNotFoundException, BlogNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(userService.addBlogToSavedBlogPosts(userId, blogId));
    }

    @DeleteMapping("/{userId}/savedBlogs/{blogId}")
    public ResponseEntity<User> removeBlogFromSavedBlogPosts(@PathVariable Long userId, @PathVariable Long blogId) throws UserNotFoundException, BlogNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(userService.removeBlogFromSavedBlogPosts(userId, blogId));
    }

    @GetMapping("/{userId}/savedBlogs/contains/{blogId}")
    public ResponseEntity<Boolean> checkIfUserSavedBlog(@PathVariable Long userId, @PathVariable Long blogId) {
        boolean doesContain = userService.checkIfUserSavedBlog(userId, blogId);
        return ResponseEntity.status(HttpStatus.OK).body(doesContain);
    }

    @GetMapping("/{userId}/savedBlogs/count")
    public long countSavedBlogs(@PathVariable Long userId) throws UserNotFoundException {
        return userService.countSavedBlogsByUser(userId);
    }

    @PostMapping("/{userId}/savedPosts/{postId}")
    public ResponseEntity<User> addPostToSavedPosts(@PathVariable Long userId, @PathVariable Long postId) throws PostNotFoundException, UserNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(userService.addPostToSavedPosts(userId, postId));
    }

    @DeleteMapping("/{userId}/savedPosts/{postId}")
    public ResponseEntity<User> removePostFromSavedPosts(@PathVariable Long userId, @PathVariable Long postId) throws PostNotFoundException, UserNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(userService.removePostFromSavedPosts(userId, postId));
    }

    @GetMapping("/{userId}/savedPosts/list")
    public ResponseEntity<Set<Post>> getSavedPosts(@PathVariable Long userId) throws UserNotFoundException {
        Set<Post> savedPosts = userService.getSavedPosts(userId);
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
    public ResponseEntity<Boolean> checkIfUserSavedPost(@PathVariable Long userId, @PathVariable Long postId) {
        boolean doesContain = userService.checkIfUserSavedPost(userId, postId);
        return ResponseEntity.status(HttpStatus.OK).body(doesContain);
    }

    @GetMapping("/friend-requests")
    public ResponseEntity<Set<User>> getFriendRequests() throws UserNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(userService.getFriendRequests());
    }

    @GetMapping("/current-user-friend-requests")
    public ResponseEntity<Page<User>> getCurrentFriendRequests(Pageable pageable) throws UserNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(userService.getCurrentFriendRequests(pageable));
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

    @GetMapping("/{userId}/friend-requests/total")
    public ResponseEntity<Long> countFriendRequestsByUserId(@PathVariable Long userId){
        return ResponseEntity.status(HttpStatus.OK).body(userService.countFriendRequestsByUserId(userId));
    }

    @GetMapping("/friends")
    public Set<User> getFriends() throws UserNotFoundException {
        return userService.getFriends();
    }

    @GetMapping("/current-user-friends")
    public Page<User> getCurrentUserFriends(Pageable pageable) throws UserNotFoundException {
        return userService.getCurrentUserFriends(pageable);
    }

    @GetMapping("/{userId}/friends")
    public Page<User> getFriendsById(@PathVariable Long userId, Pageable pageable) throws UserNotFoundException {
        return userService.getFriends(userId, pageable);
    }

    @GetMapping("/{userId}/friends/total")
    public ResponseEntity<Long> countFriendsByUserId(@PathVariable Long userId){
        return ResponseEntity.status(HttpStatus.OK).body(userService.countFriendsByUserId(userId));
    }

    @DeleteMapping("/friends/{friendId}")
    public void removeFriend(@PathVariable Long friendId) throws UserNotFoundException {
        userService.removeFriend(friendId);
    }

    @GetMapping("/friends/{friendId}")
    public ResponseEntity<Boolean> checkFriendship(@PathVariable Long friendId) throws UserNotFoundException {
        boolean areFriends = userService.checkFriendship(friendId);
        return ResponseEntity.ok(areFriends);
    }

    @GetMapping("/{receptorUserId}/requests/{emissorUserId}")
    public ResponseEntity<Boolean> checkIfSentFriendRequest(@PathVariable Long receptorUserId, @PathVariable Long emissorUserId) throws UserNotFoundException {
        boolean sentFriendRequest = userService.checkIfSentFriendRequest(receptorUserId, emissorUserId);
        return ResponseEntity.ok(sentFriendRequest);
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
    public ResponseEntity<User> addCourseToSubscribedOnlineCourses(@PathVariable Long userId, @PathVariable Long onlineCourseId) throws CourseNotFoundException, UserNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(userService.addCourseToSubscribedOnlineCourses(userId, onlineCourseId));
    }

    @DeleteMapping("/{userId}/subscribedOnlineCourses/{onlineCourseId}")
    public ResponseEntity<User> removeCourseFromSubscribedOnlineCourses(@PathVariable Long userId, @PathVariable Long onlineCourseId) throws CourseNotFoundException, UserNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(userService.removeCourseFromSubscribedOnlineCourses(userId, onlineCourseId));
    }

    @GetMapping("/{userId}/subscribedOnlineCourses/contains/{onlineCourseId}")
    public ResponseEntity<Boolean> doesUserSubscribedOnlineCourse(@PathVariable Long userId, @PathVariable Long onlineCourseId) {
        boolean doesContain = userService.doesUserSubscribedOnlineCourse(userId, onlineCourseId);
        return ResponseEntity.status(HttpStatus.OK).body(doesContain);
    }

    @GetMapping("/{userId}/groups")
    public Page<Group> getGroupsByUserId(@PathVariable Long userId, Pageable pageable) throws UserNotFoundException {
        return userService.getGroupsByUserId(userId, pageable);
    }

    @GetMapping("/{userId}/groups/total")
    public ResponseEntity<Long> countGroupsByUserId(@PathVariable Long userId){
        return ResponseEntity.status(HttpStatus.OK).body(userService.countGroupsByUserId(userId));
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
