package com.fabiocondo.controller;

import com.fabiocondo.domain.HttpResponse;
import com.fabiocondo.domain.Teacher;
import com.fabiocondo.exception.domain.CombinationExistException;
import com.fabiocondo.exception.domain.InstituicaoNotFoundException;
import com.fabiocondo.exception.domain.SubjectNotFoundException;
import com.fabiocondo.exception.domain.TeacherNotFoundException;
import com.fabiocondo.service.impl.TeacherServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/teachers")
public class TeacherController {

    public TeacherServiceImpl teacherServiceImpl;

    @Autowired
    public TeacherController(TeacherServiceImpl teacherServiceImpl) {
        this.teacherServiceImpl = teacherServiceImpl;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Teacher> findById(@PathVariable("id") Long id) throws TeacherNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(teacherServiceImpl.findById(id));
    }

    @GetMapping
    public ResponseEntity<Page<Teacher>> findAll(Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(teacherServiceImpl.findAll(pageable));
    }

    @GetMapping("/filter")
    public ResponseEntity<Page<Teacher>> findByName(@RequestParam(required = false, defaultValue = "") String name, Pageable pageable){
        return ResponseEntity.status(HttpStatus.OK).body(teacherServiceImpl.findByName(name, pageable));
    }

    @PostMapping
    public ResponseEntity<Teacher> save(@RequestParam("name") String name,
                                        @RequestParam("email") String email,
                                        @RequestParam("contactNumber") String contactNumber,
                                        @RequestParam("file") MultipartFile file) {

        return ResponseEntity.status(HttpStatus.OK).body(teacherServiceImpl.save(name, email, contactNumber, file));
    }

    @PutMapping
    public ResponseEntity<Teacher> update(@RequestParam("id") Long id,
                                          @RequestParam("name") String name,
                                          @RequestParam("email") String email,
                                          @RequestParam("contactNumber") String contactNumber,
                                          @RequestParam(value = "file", required = false) MultipartFile file) throws InstituicaoNotFoundException, TeacherNotFoundException {

        return ResponseEntity.status(HttpStatus.OK).body(teacherServiceImpl.update(id, name, email, contactNumber, file));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") Long id) throws TeacherNotFoundException {
        teacherServiceImpl.delete(id);
        return response(HttpStatus.OK, "Teacher deleted successfully");
    }

    @PostMapping("/{teacherId}/subjects/{subjectId}")
    public Teacher addSubjectToTeacherSubjectsList(@PathVariable Long teacherId, @PathVariable Long subjectId) throws TeacherNotFoundException, SubjectNotFoundException, CombinationExistException {
        return teacherServiceImpl.addSubjectToTeacherSubjectsList(teacherId, subjectId);
    }

    @PutMapping("/{teacherId}/subjects/{subjectId}")
    public Teacher removeSubjectFromTeacherSubjectsList(@PathVariable Long teacherId, @PathVariable Long subjectId) throws TeacherNotFoundException, SubjectNotFoundException {
        return teacherServiceImpl.removeSubjectFromTeacherSubjectsList(teacherId, subjectId);
    }

    @GetMapping("/total")
    public ResponseEntity<Long> getTotal(){
        return ResponseEntity.status(HttpStatus.OK).body(teacherServiceImpl.getTotal());
    }

    private ResponseEntity<HttpResponse> response(HttpStatus httpStatus, String message) {
        return new ResponseEntity<>(
                new HttpResponse(httpStatus.value(), httpStatus, httpStatus.getReasonPhrase().toUpperCase(), message),
                httpStatus);
    }
}
