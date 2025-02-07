package com.fabiocondo.controller;

import com.fabiocondo.domain.HttpResponse;
import com.fabiocondo.dto.CompetitionQuestionStatisticsDTO;
import com.fabiocondo.dto.QuizQuestionStatisticsDTO;
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

    @GetMapping("/{questionId}/quizzes")
    public ResponseEntity<QuizQuestionStatisticsDTO> getQuizStatisticsByQuestionId(@PathVariable Long questionId) {
        QuizQuestionStatisticsDTO statistics = questionServiceStatistics.getQuizStatisticsByQuestionId(questionId);
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/{questionId}/competitions")
    public ResponseEntity<CompetitionQuestionStatisticsDTO> getCompetitionStatisticsByQuestionId(@PathVariable Long questionId) {
        CompetitionQuestionStatisticsDTO statistics = questionServiceStatistics.getCompetitionStatisticsByQuestionId(questionId);
        return ResponseEntity.ok(statistics);
    }

    // Endpoint para obter estatísticas de todas as questões
    //@GetMapping("/all")
    //public ResponseEntity<List<CompetitionQuestionStatisticsDTO>> getAllStatistics() {
    //    List<CompetitionQuestionStatisticsDTO> statistics = questionServiceStatistics.getAllQuestionStatistics();
    //    return ResponseEntity.ok(statistics);
    //}

    // Endpoint para obter estatísticas agrupadas por tópico
    //@GetMapping("/by-topic")
    //public ResponseEntity<Map<String, List<CompetitionQuestionStatisticsDTO>>> getStatisticsByTopic() {
    //    Map<String, List<CompetitionQuestionStatisticsDTO>> statisticsByTopic = questionServiceStatistics.getStatisticsByTopic();
    //    return ResponseEntity.ok(statisticsByTopic);
    //}

    private ResponseEntity<HttpResponse> response(HttpStatus httpStatus, String message) {
        return new ResponseEntity<>(
                new HttpResponse(httpStatus.value(), httpStatus, httpStatus.getReasonPhrase().toUpperCase(), message),
                httpStatus);
    }
}
