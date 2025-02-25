package com.fabiocondo.controller;

import com.fabiocondo.domain.*;
import com.fabiocondo.exception.domain.*;
import com.fabiocondo.service.impl.PeriodicQuizService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/periodic-quizzes")
public class PeriodicQuizController {

    private final PeriodicQuizService periodicQuizService;

    public PeriodicQuizController(PeriodicQuizService periodicQuizService) {
        this.periodicQuizService = periodicQuizService;
    }

    @PostMapping
    public ResponseEntity<PeriodicQuiz> createPeriodicQuiz(@RequestBody PeriodicQuiz periodicQuiz,
                                                         @RequestParam Set<Long> topicIds) {
        return ResponseEntity.status(HttpStatus.OK).body(periodicQuizService.createPeriodicQuiz(periodicQuiz, topicIds));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PeriodicQuiz> updatePeriodicQuiz(@PathVariable("id") Long id,
                                                         @RequestBody PeriodicQuiz periodicQuiz,
                                                         @RequestParam Set<Long> topicIds,
                                                         @RequestParam Boolean generateQuestions) throws QuizNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(periodicQuizService.updatePeriodicQuiz(id, periodicQuiz, topicIds, generateQuestions));
    }

    //@GetMapping("/filter")
    //public Page<PeriodicQuiz> filter(PeriodicQuizFilter periodicQuizFilter, Pageable pageable) {
    //    return periodicQuizService.filter(periodicQuizFilter, pageable);
    //}

    @GetMapping("/findAll")
    public ResponseEntity<Page<PeriodicQuiz>> findAll(@RequestParam(required = false, defaultValue = "") String searchParam, Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(periodicQuizService.findAll(searchParam, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PeriodicQuiz> getPeriodicQuizById(@PathVariable Long id) throws QuizNotFoundException {
        return ResponseEntity.ok().body(periodicQuizService.getPeriodicQuizById(id));
    }

    @GetMapping("/find-by-quizId/{quizId}")
    public ResponseEntity<PeriodicQuiz> findPeriodicQuizByQuizId(@PathVariable("quizId") String quizId) throws QuizNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(periodicQuizService.findPeriodicQuizByQuizId(quizId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") Long id) throws CompetitionNotFoundException, QuizNotFoundException {
        periodicQuizService.delete(id);
        return response(HttpStatus.OK, "Quiz deleted successfully");
    }

    @GetMapping("/total")
    public ResponseEntity<Long> getTotal(){
        return ResponseEntity.status(HttpStatus.OK).body(periodicQuizService.getTotal());
    }

    @GetMapping("/{quizId}/topics")
    public ResponseEntity<Set<Topic>> getTopicsByCompetitionId(@PathVariable Long quizId) {
        Set<Topic> topics = periodicQuizService.getTopicsByCompetitionId(quizId);
        return ResponseEntity.ok(topics);
    }

    private ResponseEntity<HttpResponse> response(HttpStatus httpStatus, String message) {
        return new ResponseEntity<>(
                new HttpResponse(httpStatus.value(), httpStatus, httpStatus.getReasonPhrase().toUpperCase(), message),
                httpStatus);
    }
}

