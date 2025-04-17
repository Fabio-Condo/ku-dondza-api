package com.fabiocondo.controller;

import com.fabiocondo.domain.UserCourse;
import com.fabiocondo.exception.domain.CourseNotFoundException;
import com.fabiocondo.exception.domain.UserCourseNotFoundException;
import com.fabiocondo.exception.domain.UserNotFoundException;
import com.fabiocondo.service.impl.UserCourseService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user-courses")
public class UserCourseController {

    private final UserCourseService userCourseService;

    public UserCourseController(UserCourseService userCourseService) {
        this.userCourseService = userCourseService;
    }

    @PostMapping
    public ResponseEntity<UserCourse> addCourseToUser(@RequestBody UserCourse userCourse) {
        return ResponseEntity.ok(userCourseService.addCourseToUser(userCourse));
    }

    @PutMapping("/{userCourseId}")
    public ResponseEntity<UserCourse> updateUserCourse(@PathVariable("userCourseId") Long userCourseId, @RequestBody UserCourse userCourse) throws UserCourseNotFoundException {
        return ResponseEntity.ok(userCourseService.updateUserCourse(userCourseId, userCourse));
    }

    @DeleteMapping("/{userCourseId}")
    public ResponseEntity<Void> removeUserCourse(@PathVariable Long userCourseId) throws UserCourseNotFoundException {
        userCourseService.removeUserCourse(userCourseId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<UserCourse>> getCoursesByUser(@PathVariable Long userId, Pageable pageable) {
        return ResponseEntity.ok(userCourseService.getCoursesWithDatesByUser(userId, pageable));
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<Page<UserCourse>> getUsersByCourse(@PathVariable Long courseId, Pageable pageable) {
        return ResponseEntity.ok(userCourseService.getUsersByCourse(courseId, pageable));
    }

    @GetMapping("/check-enrollment")
    public ResponseEntity<Boolean> checkEnrollment(@RequestParam Long userId, @RequestParam Long courseId) throws UserNotFoundException, CourseNotFoundException {
        boolean isEnrolled = userCourseService.isUserEnrolledInCourse(userId, courseId);
        return ResponseEntity.ok(isEnrolled);
    }
}
