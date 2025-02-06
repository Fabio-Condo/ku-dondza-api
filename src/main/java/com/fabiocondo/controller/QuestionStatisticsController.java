package com.fabiocondo.controller;

import com.fabiocondo.domain.HttpResponse;
import com.fabiocondo.dto.QuestionStatisticsDTO;
import com.fabiocondo.service.impl.QuestionServiceStatistics;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/questions-statistics")
public class QuestionStatisticsController {

    private final QuestionServiceStatistics questionServiceStatistics;

    public QuestionStatisticsController(QuestionServiceStatistics questionServiceStatistics) {
        this.questionServiceStatistics = questionServiceStatistics;
    }

    // Endpoint para obter as estatísticas de uma questão específica por ID
    @GetMapping("/{questionId}")
    public ResponseEntity<QuestionStatisticsDTO> getStatisticsByQuestionId(@PathVariable Long questionId) {
        QuestionStatisticsDTO statistics = questionServiceStatistics.getStatisticsByQuestionId(questionId);
        return ResponseEntity.ok(statistics);
    }

    // Endpoint para obter estatísticas de todas as questões
    @GetMapping("/all")
    public ResponseEntity<List<QuestionStatisticsDTO>> getAllStatistics() {
        List<QuestionStatisticsDTO> statistics = questionServiceStatistics.getAllQuestionStatistics();
        return ResponseEntity.ok(statistics);
    }

    // Endpoint para obter estatísticas agrupadas por tópico
    @GetMapping("/by-topic")
    public ResponseEntity<Map<String, List<QuestionStatisticsDTO>>> getStatisticsByTopic() {
        Map<String, List<QuestionStatisticsDTO>> statisticsByTopic = questionServiceStatistics.getStatisticsByTopic();
        return ResponseEntity.ok(statisticsByTopic);
    }

    private ResponseEntity<HttpResponse> response(HttpStatus httpStatus, String message) {
        return new ResponseEntity<>(
                new HttpResponse(httpStatus.value(), httpStatus, httpStatus.getReasonPhrase().toUpperCase(), message),
                httpStatus);
    }
}
