package com.fabiocondo.controller;

import com.fabiocondo.domain.HttpResponse;
import com.fabiocondo.domain.OnlineCourse;
import com.fabiocondo.domain.User;
import com.fabiocondo.exception.domain.CompetitionNotFoundException;
import com.fabiocondo.exception.domain.CourseNotFoundException;
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

    public OnlineCourseController(OnlineCourseService onlineCourseService) {
        this.onlineCourseService = onlineCourseService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<OnlineCourse> findById(@PathVariable("id") Long id) throws CourseNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(onlineCourseService.findById(id));
    }

    @GetMapping("/findAll")
    public ResponseEntity<Page<OnlineCourse>> findAll(@RequestParam(required = false, defaultValue = "") String searchParam, Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(onlineCourseService.findAll(searchParam, pageable));
    }

    @GetMapping("/filter")
    public ResponseEntity<Page<OnlineCourse>> filter(@RequestParam(required = false, defaultValue = "") String searchParam, Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(onlineCourseService.findAll(searchParam, pageable));
    }

    @PostMapping
    public ResponseEntity<OnlineCourse> save(@RequestParam("name") String name,
                                             @RequestParam("description") String description,
                                             @RequestParam("instrutor") String instrutor,
                                             @RequestParam("file") MultipartFile file) {

        return ResponseEntity.status(HttpStatus.OK).body(onlineCourseService.save(name, description, instrutor, file));
    }

    @PutMapping
    public ResponseEntity<OnlineCourse> update(@RequestParam("id") Long id,
                                               @RequestParam("name") String name,
                                               @RequestParam("description") String description,
                                               @RequestParam("instrutor") String instrutor,
                                               @RequestParam(value = "file", required = false) MultipartFile file) throws CourseNotFoundException {

        return ResponseEntity.status(HttpStatus.OK).body(onlineCourseService.update(id, name, description, instrutor, file));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") Long id) throws CourseNotFoundException {
        onlineCourseService.delete(id);
        return response(HttpStatus.OK, "Course deleted successfully");
    }

    @GetMapping("/total")
    public ResponseEntity<Long> getTotal() {
        return ResponseEntity.status(HttpStatus.OK).body(onlineCourseService.getTotal());
    }

    @GetMapping("/{courseId}/students")
    public Page<User> getStudentsByCourseId(@PathVariable Long courseId, Pageable pageable) throws CourseNotFoundException {
        return onlineCourseService.getStudentsByCourseId(courseId, pageable);
    }

    @GetMapping("/{courseId}/students/total")
    public ResponseEntity<Long> countOnlineCourseStudentsByCourseId(@PathVariable Long courseId){
        return ResponseEntity.status(HttpStatus.OK).body(onlineCourseService.countOnlineCourseStudentsByCourseId(courseId));
    }

    private ResponseEntity<HttpResponse> response(HttpStatus httpStatus, String message) {
        return new ResponseEntity<>(
                new HttpResponse(httpStatus.value(), httpStatus, httpStatus.getReasonPhrase().toUpperCase(), message),
                httpStatus);
    }
}
