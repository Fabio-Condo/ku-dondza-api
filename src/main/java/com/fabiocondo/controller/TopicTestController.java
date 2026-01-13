package com.fabiocondo.controller;

import com.fabiocondo.domain.HttpResponse;
import com.fabiocondo.domain.Question;
import com.fabiocondo.domain.TopicTest;
import com.fabiocondo.domain.User;
import com.fabiocondo.dto.TopicWithTestsDTO;
import com.fabiocondo.dtoMapper.TopicTestMapper;
import com.fabiocondo.exception.domain.QuestionNotFoundException;
import com.fabiocondo.exception.domain.SubjectNotFoundException;
import com.fabiocondo.exception.domain.TopicNotFoundException;
import com.fabiocondo.exception.domain.UserNotFoundException;
import com.fabiocondo.repository.TopicTestRepository;
import com.fabiocondo.service.impl.TopicTestService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/topic-tests")
public class TopicTestController {

    public final TopicTestService topicTestService;

    public final TopicTestMapper topicTestMapper;

    public final TopicTestRepository topicTestRepository;


    public TopicTestController(TopicTestService topicTestService, TopicTestMapper topicTestMapper, TopicTestRepository topicTestRepository) {
        this.topicTestService = topicTestService;
        this.topicTestMapper = topicTestMapper;
        this.topicTestRepository = topicTestRepository;
    }

    @GetMapping("/{id}")
    public ResponseEntity<TopicTest> findById(@PathVariable("id") Long id) throws TopicNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(topicTestService.findById(id));
    }

    @PostMapping
    public ResponseEntity<TopicTest> save(@RequestBody TopicTest topicTest) throws TopicNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(topicTestService.save(topicTest));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TopicTest> update(@PathVariable("id") Long id, @RequestBody TopicTest topicTest) throws TopicNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(topicTestService.update(topicTest, id));
    }

    @GetMapping
    public ResponseEntity<List<TopicTest>> findAll() {
        return ResponseEntity.status(HttpStatus.OK).body(topicTestService.findAll());
    }

    //@GetMapping("/{id}/subjects")
    //public List<TopicTest> getBySubjectId(@PathVariable Long id) {
    //    return topicTestService.getBySubjectId(id);
    //}

    @GetMapping("/{subjectId}/subjects/users/{userId}")
    public List<TopicWithTestsDTO> getTopicsWithTestsBySubject(@PathVariable("subjectId") Long subjectId, @PathVariable("userId") Long userId) {

        // Busca todos os TopicTest da disciplina
        List<TopicTest> tests = topicTestRepository.findBySubjectId(subjectId);

        // Agrupa por tópico usando o mapper
        return topicTestMapper.groupByTopic(tests, userId);
    }

    //@GetMapping("/{subjectId}/subjects/users/{userId}")
    //public List<TopicWithTestsDTO> getTopicsWithTests22(
    //        @PathVariable Long subjectId,
    //        @PathVariable Long userId) {
    //    List<TopicTest> tests = topicTestService.getTopicTestsWithUserQuizzes(subjectId, userId);
    //    return topicTestMapper.groupByTopic(tests, userId);
    //}

    //@GetMapping("/{id}/subjects")
    //public List<TopicTestDTO> getTestsBySubject(@PathVariable("id") Long subjectId) {
    //    List<TopicTest> tests = topicTestRepository.findBySubjectId(subjectId);
    //    return topicTestMapper.toDTOListOrdered(tests);
    //}

    //@GetMapping("/{id}/subjects")
    //public List<TopicTest> getProgressForSubject2(Long subjectId) {
    //    return topicTestRepository.findBySubjectId(subjectId);
    //}

    @GetMapping("/total")
    public ResponseEntity<Long> getTotal(){
        return ResponseEntity.status(HttpStatus.OK).body(topicTestService.getTotal());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") Long id) throws TopicNotFoundException {
        topicTestService.delete(id);
        return response(HttpStatus.OK, "Topic deleted successfully");
    }

    @GetMapping("/{id}/questions")
    public ResponseEntity<Set<Question>> getQuestionsByTopicTestId(@PathVariable("id") Long topicTestId) throws TopicNotFoundException {
        Set<Question> questions = topicTestService.getQuestionsByTopicTestId(topicTestId);
        return ResponseEntity.ok(questions);
    }

    @PostMapping("/{topicTestId}/questions/{questionId}")
    public ResponseEntity<TopicTest> addQuestionToTopicTestQuestions(@PathVariable Long topicTestId, @PathVariable Long questionId) throws TopicNotFoundException, QuestionNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(topicTestService.addQuestionToTopicTestQuestions(topicTestId, questionId));
    }

    @DeleteMapping("/{topicTestId}/questions/{questionId}")
    public ResponseEntity<TopicTest> c(@PathVariable Long topicTestId, @PathVariable Long questionId) throws TopicNotFoundException, QuestionNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(topicTestService.removeQuestionFromTopicTestQuestions(topicTestId, questionId));
    }

    //@GetMapping("/{topicTestId}/users/{userId}")
    //public ResponseEntity<Optional<Quiz>> getQuizByUserAndTopicTest(
    //        @PathVariable Long topicTestId,
    //        @PathVariable Long userId) {

    //    return ResponseEntity.status(HttpStatus.OK).body(topicTestService.getQuizByUserAndTopicTest(topicTestId, userId));
    //}

    private ResponseEntity<HttpResponse> response(HttpStatus httpStatus, String message) {
        return new ResponseEntity<>(
                new HttpResponse(httpStatus.value(), httpStatus, httpStatus.getReasonPhrase().toUpperCase(), message),
                httpStatus);
    }
}