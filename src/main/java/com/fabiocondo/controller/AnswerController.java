package com.fabiocondo.controller;

import com.fabiocondo.domain.Answer;
import com.fabiocondo.domain.HttpResponse;
import com.fabiocondo.exception.domain.QuestionNotFoundException;
import com.fabiocondo.service.impl.AnswerServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/answers")
public class AnswerController {

    public AnswerServiceImpl answerService;

    public AnswerController(AnswerServiceImpl answerService) {
        this.answerService = answerService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Answer> findById(@PathVariable("id") Long id) throws QuestionNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(answerService.findById(id));
    }

    @GetMapping("/list")
    public ResponseEntity<List<Answer>> findAll() {
        return ResponseEntity.status(HttpStatus.OK).body(answerService.findAll());
    }

    private ResponseEntity<HttpResponse> response(HttpStatus httpStatus, String message) {
        return new ResponseEntity<>(
                new HttpResponse(httpStatus.value(), httpStatus, httpStatus.getReasonPhrase().toUpperCase(), message),
                httpStatus);
    }
}
