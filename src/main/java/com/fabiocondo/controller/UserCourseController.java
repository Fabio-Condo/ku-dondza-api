package com.fabiocondo.controller;

import com.fabiocondo.domain.User;
import com.fabiocondo.domain.UserCourse;
import com.fabiocondo.dto.UserDTO;
import com.fabiocondo.dtoMapper.UserMapper;
import com.fabiocondo.exception.domain.CourseNotFoundException;
import com.fabiocondo.exception.domain.UserCourseNotFoundException;
import com.fabiocondo.exception.domain.UserNotFoundException;
import com.fabiocondo.service.impl.CourseService;
import com.fabiocondo.service.impl.UserCourseService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user-courses")
public class UserCourseController {

    private final UserCourseService userCourseService;
    public final CourseService courseService;
    private final UserMapper userMapper;

    public UserCourseController(UserCourseService userCourseService, CourseService courseService, UserMapper userMapper) {
        this.userCourseService = userCourseService;
        this.courseService = courseService;
        this.userMapper = userMapper;
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
        return ResponseEntity.ok(userCourseService.getCoursesByUser(userId, pageable));
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

    @GetMapping("/{courseId}/enrolled-users")
    public ResponseEntity<Page<UserDTO>> getEnrolledUsersByCourseId(@PathVariable Long courseId, Pageable pageable) throws CourseNotFoundException {
        Page<User> students = courseService.getStudentsByCourseId(courseId, pageable);
        Page<UserDTO> userDTOs = userMapper.domainPageToDTOPage(students, pageable);

        userDTOs.forEach(dto -> {
            try {
                double progress = courseService.calculateUserProgressInCourse(dto.getId(), courseId);
                dto.setMarkedContentRate(progress);
            } catch (UserNotFoundException e) {
                dto.setMarkedContentRate(0);
            }
        });

        return ResponseEntity.ok(userDTOs);
    }
}
