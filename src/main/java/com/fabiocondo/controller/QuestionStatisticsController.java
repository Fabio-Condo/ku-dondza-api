package com.fabiocondo.controller;

import com.fabiocondo.domain.HttpResponse;
import com.fabiocondo.dto.QuizQuestionStatisticsDTO;
import com.fabiocondo.service.impl.QuestionServiceStatistics;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/questions-statistics")
public class QuestionStatisticsController {

    private final QuestionServiceStatistics questionServiceStatistics;

    public QuestionStatisticsController(QuestionServiceStatistics questionServiceStatistics) {
        this.questionServiceStatistics = questionServiceStatistics;
    }

    @GetMapping("/{questionId}/quizzes")
    public ResponseEntity<QuizQuestionStatisticsDTO> getQuizStatisticsByQuestionId(@PathVariable Long questionId) {
        QuizQuestionStatisticsDTO statistics = questionServiceStatistics.getQuizStatisticsByQuestionId(questionId);
        return ResponseEntity.ok(statistics);
    }

    private ResponseEntity<HttpResponse> response(HttpStatus httpStatus, String message) {
        return new ResponseEntity<>(
                new HttpResponse(httpStatus.value(), httpStatus, httpStatus.getReasonPhrase().toUpperCase(), message),
                httpStatus);
    }
}
