package com.fabiocondo.controller;

import com.fabiocondo.domain.User;
import com.fabiocondo.domain.UserSubjectSubscription;
import com.fabiocondo.dto.UserDTO;
import com.fabiocondo.dtoMapper.UserMapper;
import com.fabiocondo.exception.domain.SubjectNotFoundException;
import com.fabiocondo.exception.domain.SubscriptionExistException;
import com.fabiocondo.exception.domain.UserNotFoundException;
import com.fabiocondo.exception.domain.UserSubjectSubscriptionNotFoundException;
import com.fabiocondo.service.impl.SubjectServiceImpl;
import com.fabiocondo.service.impl.UserSubjectSubscriptionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user-subjects-subscription")
public class UserSubjectSubscriptionController {

    private final UserSubjectSubscriptionService userSubjectSubscriptionService;
    public final SubjectServiceImpl subjectService;
    private final UserMapper userMapper;

    public UserSubjectSubscriptionController(UserSubjectSubscriptionService userSubjectSubscriptionService, SubjectServiceImpl subjectService, UserMapper userMapper) {
        this.userSubjectSubscriptionService = userSubjectSubscriptionService;
        this.subjectService = subjectService;
        this.userMapper = userMapper;
    }

    @PostMapping
    public ResponseEntity<UserSubjectSubscription> addSubjectToUser(@RequestBody UserSubjectSubscription userSubjectSubscription) throws UserNotFoundException, SubjectNotFoundException, SubscriptionExistException {
        return ResponseEntity.ok(userSubjectSubscriptionService.addSubjectToUser(userSubjectSubscription));
    }

    @PutMapping("/{userSubjectSubscriptionId}")
    public ResponseEntity<UserSubjectSubscription> updateUserSubjectSubscription(@PathVariable("userSubjectSubscriptionId") Long userSubjectSubscriptionId, @RequestBody UserSubjectSubscription userSubjectSubscription) throws UserSubjectSubscriptionNotFoundException {
        return ResponseEntity.ok(userSubjectSubscriptionService.updateUserSubjectSubscription(userSubjectSubscriptionId, userSubjectSubscription));
    }

    @DeleteMapping("/{userSubjectSubscriptionId}")
    public ResponseEntity<Void> removeSubscription(@PathVariable Long userSubjectSubscriptionId) throws UserSubjectSubscriptionNotFoundException {
        userSubjectSubscriptionService.removeSubscription(userSubjectSubscriptionId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<UserSubjectSubscription>> getSubjectsByUser(@PathVariable Long userId, Pageable pageable) {
        return ResponseEntity.ok(userSubjectSubscriptionService.getSubjectsByUser(userId, pageable));
    }

    @GetMapping("/subject/{subjectId}")
    public ResponseEntity<Page<UserSubjectSubscription>> getUsersBySubject(@PathVariable Long subjectId, Pageable pageable) {
        return ResponseEntity.ok(userSubjectSubscriptionService.getUsersBySubject(subjectId, pageable));
    }

    @GetMapping("/check-enrollment")
    public ResponseEntity<Boolean> checkEnrollment(@RequestParam Long userId, @RequestParam Long subjectId) throws UserNotFoundException, SubjectNotFoundException {
        boolean isEnrolled = userSubjectSubscriptionService.isUserEnrolledInSubject(userId, subjectId);
        return ResponseEntity.ok(isEnrolled);
    }

    @GetMapping("/{subjectId}/enrolled-users")
    public ResponseEntity<Page<UserDTO>> getEnrolledUsersBySubjectId(@PathVariable Long subjectId, Pageable pageable) throws SubjectNotFoundException {
        Page<User> students = subjectService.getStudentsByCourseId(subjectId, pageable);
        Page<UserDTO> userDTOs = userMapper.domainPageToDTOPage(students, pageable);

        userDTOs.forEach(dto -> {
            try {
                double progress = subjectService.calculateUserProgressInSubject(dto.getId(), subjectId);
                dto.setMarkedContentRate(progress);
            } catch (UserNotFoundException e) {
                dto.setMarkedContentRate(0);
            }
        });

        return ResponseEntity.ok(userDTOs);
    }
}
