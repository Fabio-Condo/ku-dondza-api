package com.fabiocondo.controller;

import com.fabiocondo.domain.Exame;
import com.fabiocondo.domain.Subject;
import com.fabiocondo.exception.domain.ExameNotFoundException;
import com.fabiocondo.exception.domain.SubjectNotFoundException;
import com.fabiocondo.service.SubjectService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/subjects")
public class SubjectController {

    public SubjectService subjectService;

    public SubjectController(SubjectService subjectService) {
        this.subjectService = subjectService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Subject> findById(@PathVariable("id") Long id) throws SubjectNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(subjectService.findById(id));
    }

    @GetMapping
    public ResponseEntity<List<Subject>> findAll() throws SubjectNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(subjectService.findAll());
    }

    @PostMapping
    public ResponseEntity<Subject> save(@RequestBody Subject subject) throws SubjectNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(subjectService.save(subject));
    }
}