package com.fabiocondo.controller;

import com.fabiocondo.domain.HttpResponse;
import com.fabiocondo.domain.Question;
import com.fabiocondo.domain.Quiz;
import com.fabiocondo.exception.domain.QuizNotFoundException;
import com.fabiocondo.repository.filter.QuizFilter;
import com.fabiocondo.service.impl.QuizService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

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

    @GetMapping("/find-by-quizId/{quizId}")
    public ResponseEntity<Quiz> findQuizByQuizId(@PathVariable("quizId") String quizId) throws QuizNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(quizService.findQuizByQuizId(quizId));
    }

    @GetMapping
    public ResponseEntity<Page<Quiz>> findAll(@RequestParam(required = false, defaultValue = "") String searchParam, Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(quizService.findAll(searchParam, pageable));
    }

    @GetMapping("/filter")
    public Page<Quiz> filter(QuizFilter quizFilter, Pageable pageable) {
        return quizService.filter(quizFilter, pageable);
    }

    @GetMapping("/findAll")
    public ResponseEntity<List<Quiz>> findAll() {
        List<Quiz> quizzes = quizService.findAll();
        return ResponseEntity.ok(quizzes);
    }

    @PostMapping
    public ResponseEntity<Quiz> createQuiz(@RequestBody Quiz quiz, @RequestParam Set<Long> questionIds) {
        Quiz savedQuiz = quizService.saveQuizWithQuestions(quiz, questionIds);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedQuiz);
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

    @GetMapping("/{quizId}/questions")
    public Page<Question> getQuestionsByQuizId(@PathVariable Long quizId, Pageable pageable) throws QuizNotFoundException {
        return quizService.getQuestionsByQuizId(quizId, pageable);
    }

    @GetMapping("/{quizId}/questions/total")
    public ResponseEntity<Long> countQuestionsByQuizId(@PathVariable Long quizId){
        return ResponseEntity.status(HttpStatus.OK).body(quizService.countQuestionsByQuizId(quizId));
    }

    private ResponseEntity<HttpResponse> response(HttpStatus httpStatus, String message) {
        return new ResponseEntity<>(
                new HttpResponse(httpStatus.value(), httpStatus, httpStatus.getReasonPhrase().toUpperCase(), message),
                httpStatus);
    }

}
