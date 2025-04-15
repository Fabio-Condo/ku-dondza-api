package com.fabiocondo.controller;

import com.fabiocondo.domain.*;
import com.fabiocondo.dto.OnlineCourseDTO;
import com.fabiocondo.dto.UserDTO;
import com.fabiocondo.dtoMapper.OnlineCourseMapper;
import com.fabiocondo.exception.domain.OnlineCourseNotFoundException;
import com.fabiocondo.exception.domain.UserNotFoundException;
import com.fabiocondo.repository.filter.OnlineCourseFilter;
import com.fabiocondo.service.impl.OnlineCourseService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/online-course")
public class OnlineCourseController {

    public OnlineCourseService onlineCourseService;
    private final OnlineCourseMapper onlineCourseMapper;


    public OnlineCourseController(OnlineCourseService onlineCourseService, OnlineCourseMapper onlineCourseMapper) {
        this.onlineCourseService = onlineCourseService;
        this.onlineCourseMapper = onlineCourseMapper;
    }

    @GetMapping("/{id}")
    public ResponseEntity<OnlineCourse> findById(@PathVariable("id") Long id) throws OnlineCourseNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(onlineCourseService.findById(id));
    }

    @GetMapping("/find-by-courseId/{onlineCourseId}")
    public ResponseEntity<OnlineCourseDTO> findOnlineCourseByOnlineCourseId(@PathVariable("onlineCourseId") String onlineCourseId) throws OnlineCourseNotFoundException, UserNotFoundException {
        OnlineCourse onlineCourse = onlineCourseService.findOnlineCourseByOnlineCourseId(onlineCourseId);
        return ResponseEntity.status(HttpStatus.OK).body(onlineCourseMapper.domainToDTO_WithModules(onlineCourse));
    }

    @GetMapping("/findAll")
    public ResponseEntity<Page<OnlineCourse>> findAll(@RequestParam(required = false, defaultValue = "") String searchParam, Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(onlineCourseService.findAll(searchParam, pageable));
    }

    @GetMapping("/filter")
    public Page<OnlineCourse> filter(OnlineCourseFilter onlineCourseFilter, Pageable pageable) {
        return onlineCourseService.filter(onlineCourseFilter, pageable);
    }

    @PostMapping
    public ResponseEntity<OnlineCourse> save(@RequestParam("name") String name,
                                             @RequestParam("description") String description,
                                             @RequestParam("lunchDate") String lunchDate,
                                             @RequestParam("instrutorId") Long instrutorId,
                                             @RequestParam("file") MultipartFile file) throws UserNotFoundException {

        return ResponseEntity.status(HttpStatus.OK).body(onlineCourseService.save(name, description, lunchDate, instrutorId, file));
    }

    @PutMapping
    public ResponseEntity<OnlineCourse> update(@RequestParam("id") Long id,
                                               @RequestParam("name") String name,
                                               @RequestParam("description") String description,
                                               @RequestParam("lunchDate") String lunchDate,
                                               @RequestParam("instrutorId") Long instrutorId,
                                               @RequestParam(value = "file", required = false) MultipartFile file) throws UserNotFoundException, OnlineCourseNotFoundException {

        return ResponseEntity.status(HttpStatus.OK).body(onlineCourseService.update(id, name, description, lunchDate, instrutorId, file));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") Long id) throws OnlineCourseNotFoundException {
        onlineCourseService.delete(id);
        return response(HttpStatus.OK, "Course deleted successfully");
    }

    @GetMapping("/{courseId}/students")
    public Page<UserDTO> getStudentsByCourseId(@PathVariable Long courseId, Pageable pageable) throws OnlineCourseNotFoundException {
        return onlineCourseService.getStudentsByCourseId(courseId, pageable);
    }

    @GetMapping("/{courseId}/progress/{userId}")
    public double calculateUserProgressInCourse(@PathVariable Long courseId, @PathVariable Long userId) throws UserNotFoundException {
        return onlineCourseService.calculateUserProgressInCourse(userId, courseId);
    }

    private ResponseEntity<HttpResponse> response(HttpStatus httpStatus, String message) {
        return new ResponseEntity<>(
                new HttpResponse(httpStatus.value(), httpStatus, httpStatus.getReasonPhrase().toUpperCase(), message),
                httpStatus);
    }
}
