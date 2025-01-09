package com.fabiocondo.controller;

import com.fabiocondo.domain.*;
import com.fabiocondo.exception.domain.CourseNotFoundException;
import com.fabiocondo.exception.domain.QuestionNotFoundException;
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

    public OnlineCourseController(OnlineCourseService onlineCourseService) {
        this.onlineCourseService = onlineCourseService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<OnlineCourse> findById(@PathVariable("id") Long id) throws CourseNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(onlineCourseService.findById(id));
    }

    @GetMapping("/find-by-courseId/{onlineCourseId}")
    public ResponseEntity<OnlineCourse> findOnlineCourseByOnlineCourseId(@PathVariable("onlineCourseId") String onlineCourseId) throws CourseNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(onlineCourseService.findOnlineCourseByOnlineCourseId(onlineCourseId));
    }

    @GetMapping("/findAll")
    public ResponseEntity<Page<OnlineCourse>> findAll(@RequestParam(required = false, defaultValue = "") String searchParam, Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(onlineCourseService.findAll(searchParam, pageable));
    }

    //@GetMapping("/filter")
    //public ResponseEntity<Page<OnlineCourse>> filter(@RequestParam(required = false, defaultValue = "") String searchParam, Pageable pageable) {
    //    return ResponseEntity.status(HttpStatus.OK).body(onlineCourseService.findAll(searchParam, pageable));
    //}

    @GetMapping("/filter")
    public Page<OnlineCourse> filter(OnlineCourseFilter onlineCourseFilter, Pageable pageable) {
        return onlineCourseService.filter(onlineCourseFilter, pageable);
    }

    @PostMapping
    public ResponseEntity<OnlineCourse> save(@RequestParam("name") String name,
                                             @RequestParam("description") String description,
                                             @RequestParam("requirements") String requirements,
                                             @RequestParam("lunchDate") String lunchDate,
                                             @RequestParam("instrutorId") Long instrutorId,
                                             @RequestParam("file") MultipartFile file) throws UserNotFoundException {

        return ResponseEntity.status(HttpStatus.OK).body(onlineCourseService.save(name, description, requirements, lunchDate, instrutorId, file));
    }

    @PutMapping
    public ResponseEntity<OnlineCourse> update(@RequestParam("id") Long id,
                                               @RequestParam("name") String name,
                                               @RequestParam("description") String description,
                                               @RequestParam("requirements") String requirements,
                                               @RequestParam("lunchDate") String lunchDate,
                                               @RequestParam("instrutorId") Long instrutorId,
                                               @RequestParam(value = "file", required = false) MultipartFile file) throws CourseNotFoundException, UserNotFoundException {

        return ResponseEntity.status(HttpStatus.OK).body(onlineCourseService.update(id, name, description, requirements, lunchDate, instrutorId, file));
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

    @GetMapping("/{courseId}/questions")
    public Page<Question> getQuestionsByCourseId(@PathVariable Long courseId, Pageable pageable) throws CourseNotFoundException {
        return onlineCourseService.getQuestionsByCourseId(courseId, pageable);
    }

    @PostMapping("/{courseId}/questions/{questionId}")
    public ResponseEntity<OnlineCourse> addQuestionToCourse(@PathVariable Long courseId, @PathVariable Long questionId) throws QuestionNotFoundException, CourseNotFoundException {
        OnlineCourse updatedQuiz = onlineCourseService.addQuestionToCourse(courseId, questionId);
        return updatedQuiz != null ? ResponseEntity.ok(updatedQuiz) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{courseId}/questions/{questionId}")
    public ResponseEntity<OnlineCourse> removeQuestionFromCourse(@PathVariable Long courseId, @PathVariable Long questionId) throws QuestionNotFoundException, CourseNotFoundException {
        OnlineCourse updatedQuiz = onlineCourseService.removeQuestionFromCourse(courseId, questionId);
        return updatedQuiz != null ? ResponseEntity.ok(updatedQuiz) : ResponseEntity.notFound().build();
    }

    @GetMapping("/{courseId}/questions/total")
    public ResponseEntity<Long> countQuestionsByCourseId(@PathVariable Long courseId){
        return ResponseEntity.status(HttpStatus.OK).body(onlineCourseService.countQuestionsByCourseId(courseId));
    }

    private ResponseEntity<HttpResponse> response(HttpStatus httpStatus, String message) {
        return new ResponseEntity<>(
                new HttpResponse(httpStatus.value(), httpStatus, httpStatus.getReasonPhrase().toUpperCase(), message),
                httpStatus);
    }
}
