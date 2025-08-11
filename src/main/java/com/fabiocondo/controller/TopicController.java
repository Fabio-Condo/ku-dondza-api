package com.fabiocondo.controller;

import com.fabiocondo.domain.HttpResponse;
import com.fabiocondo.domain.Topic;
import com.fabiocondo.exception.domain.TopicNotFoundException;
import com.fabiocondo.repository.filter.TopicFilter;
import com.fabiocondo.service.impl.TopicService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/topics")
public class TopicController {

    public TopicService topicService;

    public TopicController(TopicService topicService) {
        this.topicService = topicService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Topic> findById(@PathVariable("id") Long id) throws TopicNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(topicService.findById(id));
    }

    @GetMapping("/find-by-topicId/{topicId}")
    public ResponseEntity<Topic> findTopicByTopicId(@PathVariable("topicId") String topicId) throws TopicNotFoundException {
        Topic topic = topicService.findTopicByTopicId(topicId);
        return ResponseEntity.status(HttpStatus.OK).body(topic);
    }

    @PostMapping
    public ResponseEntity<Topic> save(@RequestBody Topic Topic) throws TopicNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(topicService.save(Topic));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Topic> update(@PathVariable("id") Long id, @RequestBody Topic topic) throws TopicNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(topicService.update(topic, id));
    }

    @GetMapping("/filter")
    public Page<Topic> filter(TopicFilter topicFilter, Pageable pageable) {
        return topicService.filter(topicFilter, pageable);
    }

    //@GetMapping("/filter")
    //public ResponseEntity<Page<Topic>> findAll(Pageable pageable) {
    //    return ResponseEntity.status(HttpStatus.OK).body(topicService.findAll(pageable));
    //}

    @GetMapping
    public ResponseEntity<List<Topic>> findAll() {
        return ResponseEntity.status(HttpStatus.OK).body(topicService.findAll());
    }

    @GetMapping("/{id}/subjects")
    public List<Topic> getBySubjectId(@PathVariable Long id) {
        return topicService.getBySubjectId(id);
    }

    @GetMapping("/total")
    public ResponseEntity<Long> getTotal(){
        return ResponseEntity.status(HttpStatus.OK).body(topicService.getTotal());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") Long id) throws TopicNotFoundException {
        topicService.delete(id);
        return response(HttpStatus.OK, "Topic deleted successfully");
    }

    private ResponseEntity<HttpResponse> response(HttpStatus httpStatus, String message) {
        return new ResponseEntity<>(
                new HttpResponse(httpStatus.value(), httpStatus, httpStatus.getReasonPhrase().toUpperCase(), message),
                httpStatus);
    }
}