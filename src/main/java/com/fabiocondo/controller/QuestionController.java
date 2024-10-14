package com.fabiocondo.controller;

import com.fabiocondo.domain.HttpResponse;
import com.fabiocondo.domain.Question;
import com.fabiocondo.exception.domain.QuestionNotFoundException;
import com.fabiocondo.service.impl.QuestionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/questions")
public class QuestionController {

    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Question> findById(@PathVariable("id") Long id) throws QuestionNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(questionService.findById(id));
    }

    @GetMapping
    public ResponseEntity<Page<Question>> findAll(Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(questionService.findAll(pageable));
    }

    @PostMapping
    public ResponseEntity<Question> save(@RequestBody Question question) {
        Question createdQuestion = questionService.save(question);
        return ResponseEntity.ok(createdQuestion);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Question> update(@PathVariable("id") Long id, @RequestBody Question question) throws QuestionNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(questionService.update(question, id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") Long id) throws QuestionNotFoundException {
        questionService.delete(id);
        return response(HttpStatus.OK, "Question deleted successfully");
    }

    @GetMapping("/total")
    public ResponseEntity<Long> getTotal(){
        return ResponseEntity.status(HttpStatus.OK).body(questionService.getTotal());
    }

    @GetMapping("/findQuestionsByQuizId")
    public ResponseEntity<Page<Question>> findByQuizId(@RequestParam Long quizId, Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(questionService.findByQuizId(quizId, pageable));
    }

    private ResponseEntity<HttpResponse> response(HttpStatus httpStatus, String message) {
        return new ResponseEntity<>(
                new HttpResponse(httpStatus.value(), httpStatus, httpStatus.getReasonPhrase().toUpperCase(), message),
                httpStatus);
    }
}
