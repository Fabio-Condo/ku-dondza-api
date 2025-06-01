package com.fabiocondo.controller;

import com.fabiocondo.domain.*;
import com.fabiocondo.dto.CourseDTO;
import com.fabiocondo.dto.UserDTO;
import com.fabiocondo.dtoMapper.CourseMapper;
import com.fabiocondo.dtoMapper.UserMapper;
import com.fabiocondo.exception.domain.CourseNotFoundException;
import com.fabiocondo.exception.domain.UserNotFoundException;
import com.fabiocondo.repository.filter.CourseFilter;
import com.fabiocondo.service.impl.CourseService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/online-course")
public class CourseController {

    public CourseService courseService;
    private final CourseMapper courseMapper;
    private final UserMapper userMapper;


    public CourseController(CourseService courseService, CourseMapper courseMapper, UserMapper userMapper) {
        this.courseService = courseService;
        this.courseMapper = courseMapper;
        this.userMapper = userMapper;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Course> findById(@PathVariable("id") Long id) throws CourseNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(courseService.findById(id));
    }

    @GetMapping("/find-by-courseId/{onlineCourseId}")
    public ResponseEntity<CourseDTO> findOnlineCourseByOnlineCourseId(@PathVariable("onlineCourseId") String onlineCourseId, @RequestParam("currentUserId") Long currentUserId) throws CourseNotFoundException, UserNotFoundException {
        Course course = courseService.findOnlineCourseByOnlineCourseId(onlineCourseId, currentUserId);
        return ResponseEntity.status(HttpStatus.OK).body(courseMapper.domainToDTO_WithModules(course, currentUserId));
    }

    @GetMapping("/findAll")
    public ResponseEntity<Page<Course>> findAll(@RequestParam(required = false, defaultValue = "") String searchParam, Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(courseService.findAll(searchParam, pageable));
    }

    @GetMapping("/filter")
    public Page<Course> filter(CourseFilter courseFilter, Pageable pageable) {
        return courseService.filter(courseFilter, pageable);
    }

    @PostMapping
    public ResponseEntity<Course> save(@RequestParam("name") String name,
                                       @RequestParam("description") String description,
                                       @RequestParam("lunchDate") String lunchDate,
                                       @RequestParam("instrutorId") Long instrutorId,
                                       @RequestParam("file") MultipartFile file) throws UserNotFoundException {

        return ResponseEntity.status(HttpStatus.OK).body(courseService.save(name, description, lunchDate, instrutorId, file));
    }

    @PutMapping
    public ResponseEntity<Course> update(@RequestParam("id") Long id,
                                         @RequestParam("name") String name,
                                         @RequestParam("description") String description,
                                         @RequestParam("lunchDate") String lunchDate,
                                         @RequestParam("instrutorId") Long instrutorId,
                                         @RequestParam(value = "file", required = false) MultipartFile file) throws UserNotFoundException, CourseNotFoundException {

        return ResponseEntity.status(HttpStatus.OK).body(courseService.update(id, name, description, lunchDate, instrutorId, file));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") Long id) throws CourseNotFoundException {
        courseService.delete(id);
        return response(HttpStatus.OK, "Course deleted successfully");
    }

    @GetMapping("/{courseId}/students")
    public ResponseEntity<Page<UserDTO>> getStudentsByCourseId(@PathVariable Long courseId, Pageable pageable) throws CourseNotFoundException {
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


    @GetMapping("/{courseId}/progress/{userId}")
    public double calculateUserProgressInCourse(@PathVariable Long courseId, @PathVariable Long userId) throws UserNotFoundException {
        return courseService.calculateUserProgressInCourse(userId, courseId);
    }

    private ResponseEntity<HttpResponse> response(HttpStatus httpStatus, String message) {
        return new ResponseEntity<>(
                new HttpResponse(httpStatus.value(), httpStatus, httpStatus.getReasonPhrase().toUpperCase(), message),
                httpStatus);
    }
}
