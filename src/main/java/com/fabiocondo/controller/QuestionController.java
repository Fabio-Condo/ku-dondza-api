package com.fabiocondo.controller;

import com.fabiocondo.domain.*;
import com.fabiocondo.dto.QuestionDTO;
import com.fabiocondo.dtoMapper.QuestionMapper;
import com.fabiocondo.enumeration.DifficultyLevel;
import com.fabiocondo.exception.domain.InterestNotFoundException;
import com.fabiocondo.exception.domain.QuestionNotFoundException;
import com.fabiocondo.exception.domain.TopicNotFoundException;
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
    private final QuestionMapper questionMapper;

    public QuestionController(QuestionService questionService, QuestionMapper questionMapper) {
        this.questionService = questionService;
        this.questionMapper = questionMapper;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Question> findById(@PathVariable("id") Long id) throws QuestionNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(questionService.findById(id));
    }

    @GetMapping("/find-by-questionId/{questionId}")
    public ResponseEntity<QuestionDTO> findQuestionByQuestionId(@PathVariable("questionId") String questionId, @RequestParam("currentUserId") Long currentUserId) throws QuestionNotFoundException {
        Question question = questionService.findQuestionByQuestionId(questionId);
        return ResponseEntity.status(HttpStatus.OK).body(questionMapper.domainToDTO(question, currentUserId));
    }

    @GetMapping("/filter")
    public Page<QuestionDTO> filter(QuestionFilter questionFilter, @RequestParam("currentUserId") Long currentUserId, Pageable pageable) {
        return questionMapper.domainPageToDTOPage(questionService.filter(questionFilter, pageable), currentUserId, pageable);
    }

    @GetMapping
    public ResponseEntity<List<Question>> findAll() throws InterestNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(questionService.findAll());
    }

    @GetMapping("/by-topics")
    public ResponseEntity<Set<Question>> getQuestionsByTopics(@RequestParam Set<Long> topicIds,
                                                              @RequestParam DifficultyLevel difficultyLevel,
                                                              @RequestParam int limitPerTopic) {
        return ResponseEntity.ok(questionService.getQuestionsByTopics(topicIds, difficultyLevel, limitPerTopic));
    }

    //@GetMapping("/topics/{topicId}")
    //public ResponseEntity<Set<Question>> getQuestionsByTopicId(@PathVariable("topicId") Long topicId) {
    //    return ResponseEntity.ok(questionService.getQuestionsByTopicId(topicId));
    //}

    @GetMapping("/topics/{topicId}")
    public ResponseEntity<Set<QuestionDTO>> getQuestionsByTopicId(@PathVariable("topicId") Long topicId) {
        return ResponseEntity.ok(questionMapper.domainPageToDTOSet(questionService.getQuestionsByTopicId(topicId)));
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

    @PostMapping("/{questionId}/question-image") // remover
    public ResponseEntity<Question> updateQuestionImage(@PathVariable Long questionId, @RequestParam("file") MultipartFile file) throws IOException, QuestionNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(questionService.updateQuestionImage(questionId, file));
    }

    @GetMapping("/generate-from-ai")
    public Question generateQuestionFromAI(@RequestParam Long topicId, @RequestParam DifficultyLevel difficultyLevel) throws TopicNotFoundException, QuestionNotFoundException {
        return questionService.generateAdvancedQuestionFromAI(topicId, difficultyLevel);
    }

    private ResponseEntity<HttpResponse> response(HttpStatus httpStatus, String message) {
        return new ResponseEntity<>(
                new HttpResponse(httpStatus.value(), httpStatus, httpStatus.getReasonPhrase().toUpperCase(), message),
                httpStatus);
    }
}
