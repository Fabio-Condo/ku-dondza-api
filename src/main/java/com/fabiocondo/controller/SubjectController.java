package com.fabiocondo.controller;

import com.fabiocondo.domain.Subject;
import com.fabiocondo.exception.domain.SubjectNotFoundException;
import com.fabiocondo.service.impl.SubjectServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/subjects")
public class SubjectController {

    public SubjectServiceImpl subjectServiceImpl;

    public SubjectController(SubjectServiceImpl subjectServiceImpl) {
        this.subjectServiceImpl = subjectServiceImpl;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Subject> findById(@PathVariable("id") Long id) throws SubjectNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(subjectServiceImpl.findById(id));
    }

    @GetMapping
    public ResponseEntity<List<Subject>> findAll() throws SubjectNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(subjectServiceImpl.findAll());
    }

    @PostMapping
    public ResponseEntity<Subject> save(@RequestBody Subject subject) throws SubjectNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(subjectServiceImpl.save(subject));
    }
}