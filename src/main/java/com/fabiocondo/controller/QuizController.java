package com.fabiocondo.controller;

import com.fabiocondo.domain.*;
import com.fabiocondo.dto.QuizDTO;
import com.fabiocondo.dtoMapper.QuizMapper;
import com.fabiocondo.exception.domain.QuizNotFoundException;
import com.fabiocondo.exception.domain.TopicNotFoundException;
import com.fabiocondo.repository.TopicTestRepository;
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

    private final TopicTestRepository topicTestRepository;

    public QuizController(QuizService quizService, QuizMapper quizMapper, TopicTestRepository topicTestRepository) {
        this.quizService = quizService;
        this.quizMapper = quizMapper;
        this.topicTestRepository = topicTestRepository;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Quiz> findById(@PathVariable("id") Long id) throws QuizNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(quizService.findById(id));
    }

    @GetMapping("/find-by-quizId/{quizId}")
    public ResponseEntity<QuizDTO> findQuizByQuizId(@PathVariable("quizId") String quizId, @RequestParam("currentUserId") Long currentUserId) throws QuizNotFoundException {
        Quiz quiz = quizService.findQuizByQuizId(quizId);
        return ResponseEntity.status(HttpStatus.OK).body(quizMapper.domainToDTO_WithQuestionsAndAnswers(quiz, currentUserId));
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
                                               @RequestParam Set<Long> userAnswerIds,
                                               @RequestParam("currentUserId") Long currentUserId) {

        Quiz savedQuiz = quizService.saveQuizWithQuestions(quiz, questionIds, userAnswerIds);
        return ResponseEntity.status(HttpStatus.OK).body(quizMapper.domainToDTO_WithQuestionsAndAnswers(savedQuiz, currentUserId));
    }

    @PostMapping("/topic-test")
    public ResponseEntity<QuizDTO> createQuizTopicTest(@RequestBody Quiz quiz,
                                              @RequestParam Set<Long> questionIds,
                                              @RequestParam Set<Long> userAnswerIds,
                                              @RequestParam("topicTestId") Long topicTestId,
                                              @RequestParam("currentUserId") Long currentUserId) throws TopicNotFoundException {

        Quiz savedQuiz = quizService.saveQuizTopicTestWithQuestions(quiz, questionIds, userAnswerIds, topicTestId);

        QuizDTO quizDTO = quizMapper.domainToDTO(savedQuiz);

        // O quiz so eh adicionado se a taxa de acerto for de 85% para cima
        if (quizDTO.getAccuracyRate() >= 85.0){
            Test test = topicTestRepository.findById(topicTestId)
                    .orElseThrow(() -> new TopicNotFoundException("No topic test found by id: " + topicTestId));
            test.getSubmittedQuizzes().add(savedQuiz);
            topicTestRepository.save(test);
        }

        return ResponseEntity.status(HttpStatus.OK).body(quizMapper.domainToDTO_WithQuestionsAndAnswers(savedQuiz, currentUserId));
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
