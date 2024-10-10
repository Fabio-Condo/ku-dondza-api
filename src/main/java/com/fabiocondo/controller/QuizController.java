package com.fabiocondo.controller;

import com.fabiocondo.domain.HttpResponse;
import com.fabiocondo.domain.Quiz;
import com.fabiocondo.exception.domain.QuizNotFoundException;
import com.fabiocondo.service.impl.QuizService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/quizzes")
public class QuizController {

    private final QuizService quizService;

    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Quiz> findById(@PathVariable("id") Long id) throws QuizNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(quizService.findById(id));
    }

    @GetMapping
    public ResponseEntity<Page<Quiz>> findAll(Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(quizService.findAll(pageable));
    }

    @GetMapping("/findAll")
    public ResponseEntity<List<Quiz>> findAll() {
        List<Quiz> quizzes = quizService.findAll();
        return ResponseEntity.ok(quizzes);
    }

    @PostMapping
    public ResponseEntity<Quiz> save(@RequestBody Quiz quiz) {
        Quiz createdQuiz = quizService.save(quiz);
        return ResponseEntity.ok(createdQuiz);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Quiz> update(@PathVariable("id") Long id, @RequestBody Quiz quiz) throws QuizNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(quizService.update(quiz, id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") Long id) throws QuizNotFoundException {
        quizService.delete(id);
        return response(HttpStatus.OK, "Quiz deleted successfully");
    }

    @GetMapping("/total")
    public ResponseEntity<Long> getTotal(){
        return ResponseEntity.status(HttpStatus.OK).body(quizService.getTotal());
    }

    private ResponseEntity<HttpResponse> response(HttpStatus httpStatus, String message) {
        return new ResponseEntity<>(
                new HttpResponse(httpStatus.value(), httpStatus, httpStatus.getReasonPhrase().toUpperCase(), message),
                httpStatus);
    }

}
