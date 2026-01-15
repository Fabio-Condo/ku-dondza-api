package com.fabiocondo.controller;

import com.fabiocondo.domain.HttpResponse;
import com.fabiocondo.domain.Question;
import com.fabiocondo.domain.Test;
import com.fabiocondo.dto.TopicTestsDTO;
import com.fabiocondo.dtoMapper.TestMapper;
import com.fabiocondo.exception.domain.QuestionNotFoundException;
import com.fabiocondo.exception.domain.TopicNotFoundException;
import com.fabiocondo.repository.TopicTestRepository;
import com.fabiocondo.service.impl.TestService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/tests")
public class TestController {

    public final TestService testService;

    public final TestMapper testMapper;

    public final TopicTestRepository topicTestRepository;


    public TestController(TestService testService, TestMapper testMapper, TopicTestRepository topicTestRepository) {
        this.testService = testService;
        this.testMapper = testMapper;
        this.topicTestRepository = topicTestRepository;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Test> findById(@PathVariable("id") Long id) throws TopicNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(testService.findById(id));
    }

    @PostMapping
    public ResponseEntity<Test> save(@RequestBody Test test) throws TopicNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(testService.save(test));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Test> update(@PathVariable("id") Long id, @RequestBody Test test) throws TopicNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(testService.update(test, id));
    }

    @GetMapping
    public ResponseEntity<List<Test>> findAll() {
        return ResponseEntity.status(HttpStatus.OK).body(testService.findAll());
    }

    //@GetMapping("/{id}/subjects")
    //public List<Test> getBySubjectId(@PathVariable Long id) {
    //    return testService.getBySubjectId(id);
    //}

    @GetMapping("/subjects/{subjectId}/users/{userId}")
    public List<TopicTestsDTO> getTopicsWithTestsBySubject(@PathVariable("subjectId") Long subjectId, @PathVariable("userId") Long userId) {

        // Busca todos os Test da disciplina
        List<Test> tests = topicTestRepository.findBySubjectId(subjectId);

        // Agrupa por tópico usando o mapper
        return testMapper.groupByTopic(tests, userId);
    }

    //@GetMapping("/{subjectId}/subjects/users/{userId}")
    //public List<TopicTestsDTO> getTopicsWithTests22(
    //        @PathVariable Long subjectId,
    //        @PathVariable Long userId) {
    //    List<Test> tests = testService.getTopicTestsWithUserQuizzes(subjectId, userId);
    //    return testMapper.groupByTopic(tests, userId);
    //}

    //@GetMapping("/{id}/subjects")
    //public List<TestDTO> getTestsBySubject(@PathVariable("id") Long subjectId) {
    //    List<Test> tests = topicTestRepository.findBySubjectId(subjectId);
    //    return testMapper.toDTOListOrdered(tests);
    //}

    //@GetMapping("/{id}/subjects")
    //public List<Test> getProgressForSubject2(Long subjectId) {
    //    return topicTestRepository.findBySubjectId(subjectId);
    //}

    @GetMapping("/total")
    public ResponseEntity<Long> getTotal(){
        return ResponseEntity.status(HttpStatus.OK).body(testService.getTotal());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") Long id) throws TopicNotFoundException {
        testService.delete(id);
        return response(HttpStatus.OK, "Test deleted successfully");
    }

    @GetMapping("/{id}/questions")
    public ResponseEntity<Set<Question>> getQuestionsByTestId(@PathVariable("id") Long topicTestId) throws TopicNotFoundException {
        Set<Question> questions = testService.getQuestionsByTestId(topicTestId);
        return ResponseEntity.ok(questions);
    }

    @PostMapping("/{testId}/questions/{questionId}")
    public ResponseEntity<Test> addQuestionToTestQuestions(@PathVariable Long testId, @PathVariable Long questionId) throws TopicNotFoundException, QuestionNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(testService.addQuestionToTestQuestions(testId, questionId));
    }

    @DeleteMapping("/{testId}/questions/{questionId}")
    public ResponseEntity<Test> removeQuestionFromTestQuestions(@PathVariable Long testId, @PathVariable Long questionId) throws TopicNotFoundException, QuestionNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(testService.removeQuestionFromTestQuestions(testId, questionId));
    }

    //@GetMapping("/{testId}/users/{userId}")
    //public ResponseEntity<Optional<Quiz>> getQuizByUserAndTopicTest(
    //        @PathVariable Long testId,
    //        @PathVariable Long userId) {

    //    return ResponseEntity.status(HttpStatus.OK).body(testService.getQuizByUserAndTopicTest(testId, userId));
    //}

    private ResponseEntity<HttpResponse> response(HttpStatus httpStatus, String message) {
        return new ResponseEntity<>(
                new HttpResponse(httpStatus.value(), httpStatus, httpStatus.getReasonPhrase().toUpperCase(), message),
                httpStatus);
    }
}