package com.fabiocondo.controller;

import com.fabiocondo.domain.*;
import com.fabiocondo.dto.QuizDTO;
import com.fabiocondo.dtoMapper.QuizMapper;
import com.fabiocondo.exception.domain.QuizNotFoundException;
import com.fabiocondo.repository.filter.QuizFilter;
import com.fabiocondo.service.impl.QuizService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/quizzes")
public class QuizController {

    private final QuizService quizService;

    private final QuizMapper quizMapper;

    public QuizController(QuizService quizService, QuizMapper quizMapper) {
        this.quizService = quizService;
        this.quizMapper = quizMapper;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Quiz> findById(@PathVariable("id") Long id) throws QuizNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(quizService.findById(id));
    }

    @GetMapping("/find-by-quizId/{quizId}")
    public ResponseEntity<QuizDTO> findQuizByQuizId(@PathVariable("quizId") String quizId) throws QuizNotFoundException {
        Quiz quiz = quizService.findQuizByQuizId(quizId);
        return ResponseEntity.status(HttpStatus.OK).body(quizMapper.domainToDTO_WithQuestionsAndAnswers(quiz));
    }

    @GetMapping("/filter")
    public Page<QuizDTO> filter(QuizFilter quizFilter, Pageable pageable) {
        return quizMapper.domainPageToDTOPage(quizService.filter(quizFilter, pageable), pageable);
    }

    @GetMapping("/by-question/{questionId}")
    public Page<Quiz> getQuizzesByQuestionId(@PathVariable("questionId") Long questionId, Pageable pageable) throws QuizNotFoundException {
        return quizService.getQuizzesByQuestionId(questionId, pageable);
    }

    @PostMapping
    public ResponseEntity<QuizDTO> createQuiz(@RequestBody Quiz quiz,
                                           @RequestParam Set<Long> questionIds,
                                           @RequestParam Set<Long> userAnswerIds) {

        Quiz savedQuiz = quizService.saveQuizWithQuestions(quiz, questionIds, userAnswerIds);
        return ResponseEntity.status(HttpStatus.OK).body(quizMapper.domainToDTO_WithQuestionsAndAnswers(savedQuiz));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") Long id) throws QuizNotFoundException {
        quizService.delete(id);
        return response(HttpStatus.OK, "Quiz deleted successfully");
    }

    @PutMapping("/{id}/anonymous")
    public void toggleAnonymous(@PathVariable("id") Long id, @RequestBody Boolean status) throws QuizNotFoundException {
        quizService.toggleAnonymous(id, status);
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
