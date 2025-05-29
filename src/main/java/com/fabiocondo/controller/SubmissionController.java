package com.fabiocondo.controller;

import com.fabiocondo.domain.Submission;
import com.fabiocondo.exception.domain.CompetitionNotFoundException;
import com.fabiocondo.exception.domain.SubmissionNotFoundException;
import com.fabiocondo.exception.domain.UserNotFoundException;
import com.fabiocondo.service.impl.SubmissionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
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

    @GetMapping("/filter")
    public ResponseEntity<Page<Submission>> findAll(Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(submissionService.findAll(pageable));
    }

    @GetMapping("/{competitionId}/submissions")
    public ResponseEntity<Page<Submission>> findAllByCompetitionId(@PathVariable Long competitionId, Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(submissionService.findAllByCompetitionId(competitionId, pageable));
    }

    @PostMapping
    public ResponseEntity<Submission> save(@RequestBody Submission submission,
                                           @RequestParam Set<Long> userAnswerIds) throws UserNotFoundException, CompetitionNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(submissionService.create(submission, userAnswerIds));
    }

    @GetMapping("/user/{userId}/competition/{competitionId}")
    public Optional<Submission> getSubmissionByUserAndCompetition(@PathVariable Long userId, @PathVariable Long competitionId) {
        return submissionService.findSubmissionByUserAndCompetition(userId, competitionId);
    }

    @GetMapping("/{id}/total-correct-answers")
    public ResponseEntity<Long> getTotalCorrectAnswersForSubmission(@PathVariable Long id) {
        long totalCorrectAnswers = submissionService.getTotalCorrectAnswersForSubmission(id);
        return ResponseEntity.ok(totalCorrectAnswers);
    }

    // Endpoint para buscar todas as respostas de uma questão
    //@GetMapping("/by-question/{questionId}")
    //public List<AnswerWithUserDTO> getAnswersByQuestion(@PathVariable Long questionId) {
    //    return submissionService.getAnswersForQuestion(questionId);
    //}
}
