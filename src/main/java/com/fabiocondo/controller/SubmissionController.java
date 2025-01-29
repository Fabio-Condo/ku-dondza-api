package com.fabiocondo.controller;

import com.fabiocondo.domain.Submission;
import com.fabiocondo.exception.domain.CompetitionNotFoundException;
import com.fabiocondo.exception.domain.SubmissionNotFoundException;
import com.fabiocondo.exception.domain.UserNotFoundException;
import com.fabiocondo.service.impl.SubmissionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/submissions")
public class SubmissionController {

    private final SubmissionService submissionService;

    public SubmissionController(SubmissionService submissionService) {
        this.submissionService = submissionService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Submission> findById(@PathVariable("id") Long id) throws SubmissionNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(submissionService.findById(id));
    }

    @PostMapping
    public ResponseEntity<Submission> save(@RequestBody Submission submission,
                                           @RequestParam Set<Long> userAnswerIds) throws UserNotFoundException, CompetitionNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(submissionService.create(submission, userAnswerIds));
    }
}
