package com.fabiocondo.controller;

import com.fabiocondo.domain.HttpResponse;
import com.fabiocondo.domain.Teacher;
import com.fabiocondo.exception.domain.InstituicaoNotFoundException;
import com.fabiocondo.exception.domain.TeacherNotFoundException;
import com.fabiocondo.service.TeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;

@RestController
@RequestMapping("/teachers")
public class TeacherController {

    public TeacherService teacherService;

    @Autowired
    public TeacherController(TeacherService teacherService) {
        this.teacherService = teacherService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Teacher> findById(@PathVariable("id") Long id) throws TeacherNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(teacherService.findById(id));
    }

    @GetMapping
    public ResponseEntity<Page<Teacher>> findAll(Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(teacherService.findAll(pageable));
    }

    @GetMapping("/filter")
    public ResponseEntity<Page<Teacher>> findByName(@RequestParam(required = false, defaultValue = "") String name, Pageable pageable){
        return ResponseEntity.status(HttpStatus.OK).body(teacherService.findByName(name, pageable));
    }

    @PostMapping
    public ResponseEntity<Teacher> save(@RequestParam("name") String name,
                                        @RequestParam("email") String email,
                                        @RequestParam("file") MultipartFile file) {

        return ResponseEntity.status(HttpStatus.OK).body(teacherService.save(name, email, file));
    }

    @PutMapping
    public ResponseEntity<Teacher> update(@RequestParam("id") Long id,
                                          @RequestParam("name") String name,
                                          @RequestParam("email") String email,
                                          @RequestParam(value = "file", required = false) MultipartFile file) throws InstituicaoNotFoundException, TeacherNotFoundException {

        return ResponseEntity.status(HttpStatus.OK).body(teacherService.update(id, name, email, file));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") Long id) throws TeacherNotFoundException {
        teacherService.delete(id);
        return response(HttpStatus.OK, "Teacher deleted successfully");
    }

    @GetMapping("/total")
    public ResponseEntity<Long> getTotal(){
        return ResponseEntity.status(HttpStatus.OK).body(teacherService.getTotal());
    }

    private ResponseEntity<HttpResponse> response(HttpStatus httpStatus, String message) {
        return new ResponseEntity<>(
                new HttpResponse(httpStatus.value(), httpStatus, httpStatus.getReasonPhrase().toUpperCase(), message),
                httpStatus);
    }
}
