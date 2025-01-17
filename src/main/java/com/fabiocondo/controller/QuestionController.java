package com.fabiocondo.controller;

import com.fabiocondo.domain.*;
import com.fabiocondo.exception.domain.InterestNotFoundException;
import com.fabiocondo.exception.domain.QuestionNotFoundException;
import com.fabiocondo.repository.filter.QuestionFilter;
import com.fabiocondo.service.impl.QuestionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Set;

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

    @GetMapping("/find-by-questionId/{questionId}")
    public ResponseEntity<Question> findQuestionByQuestionId(@PathVariable("questionId") String questionId) throws QuestionNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(questionService.findQuestionByQuestionId(questionId));
    }

    @GetMapping("/filter")
    public Page<Question> filter(QuestionFilter questionFilter, Pageable pageable) {
        return questionService.filter(questionFilter, pageable);
    }

    @GetMapping
    public ResponseEntity<List<Question>> findAll() throws InterestNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(questionService.findAll());
    }

    @GetMapping("/by-topics")
    public ResponseEntity<List<Question>> getQuestionsByTopics(@RequestParam Set<Long> topicIds) {
        return ResponseEntity.ok(questionService.getQuestionsByTopics(topicIds));
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
    @PreAuthorize("hasAnyAuthority('user:delete')")
    public ResponseEntity<?> delete(@PathVariable("id") Long id) throws QuestionNotFoundException {
        questionService.delete(id);
        return response(HttpStatus.OK, "Question deleted successfully");
    }

    @GetMapping("/total")
    public ResponseEntity<Long> getTotal(){
        return ResponseEntity.status(HttpStatus.OK).body(questionService.getTotal());
    }

    @PostMapping("/{questionId}/question-image") // remover
    public ResponseEntity<Question> updateQuestionImage(@PathVariable Long questionId, @RequestParam("file") MultipartFile file) throws IOException, QuestionNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(questionService.updateQuestionImage(questionId, file));
    }

    private ResponseEntity<HttpResponse> response(HttpStatus httpStatus, String message) {
        return new ResponseEntity<>(
                new HttpResponse(httpStatus.value(), httpStatus, httpStatus.getReasonPhrase().toUpperCase(), message),
                httpStatus);
    }
}
