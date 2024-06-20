package com.fabiocondo.controller;

import com.fabiocondo.domain.Course;
import com.fabiocondo.domain.HttpResponse;
import com.fabiocondo.exception.domain.CourseNotFoundException;
import com.fabiocondo.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/courses")
public class CourseController {

    public CourseService courseService;

    @Autowired
    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Course> findById(@PathVariable("id") Long id) throws CourseNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(courseService.findById(id));
    }

    @GetMapping
    public ResponseEntity<Page<Course>> findAll(Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(courseService.findAll(pageable));
    }

    @GetMapping("/filter")
    public ResponseEntity<Page<Course>> findByName(@RequestParam(required = false, defaultValue = "") String name, Pageable pageable){
        return ResponseEntity.status(HttpStatus.OK).body(courseService.findByName(name, pageable));
    }

    @GetMapping("/findByInstitutionId")
    public ResponseEntity<Page<Course>>  findByInstitutionId(@RequestParam Long institutionId, Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(courseService.findByInstitutionId(institutionId, pageable));
    }

    @PostMapping
    public ResponseEntity<Course> save(@RequestBody Course course) {
        return ResponseEntity.status(HttpStatus.CREATED).body(courseService.save(course));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Course> update(@PathVariable("id") Long id, @RequestBody Course course) throws CourseNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(courseService.update(course, id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") Long id) throws CourseNotFoundException {
        courseService.delete(id);
        return response(HttpStatus.OK, "Course deleted successfully");
    }

    @GetMapping("/total")
    public ResponseEntity<Long> getTotal(){
        return ResponseEntity.status(HttpStatus.OK).body(courseService.getTotal());
    }

    private ResponseEntity<HttpResponse> response(HttpStatus httpStatus, String message) {
        return new ResponseEntity<>(
                new HttpResponse(httpStatus.value(), httpStatus, httpStatus.getReasonPhrase().toUpperCase(), message),
                httpStatus);
    }
}
