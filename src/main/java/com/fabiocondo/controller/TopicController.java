package com.fabiocondo.controller;

import com.fabiocondo.domain.Topic;
import com.fabiocondo.exception.domain.TopicNotFoundException;
import com.fabiocondo.service.impl.TopicService;
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

    @PostMapping
    public ResponseEntity<Topic> save(@RequestBody Topic Topic) throws TopicNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(topicService.save(Topic));
    }

    @GetMapping
    public ResponseEntity<List<Topic>> findAll() {
        return ResponseEntity.status(HttpStatus.OK).body(topicService.findAll());
    }

    @GetMapping("/{id}/subjects")
    public List<Topic> getBySubjectId(@PathVariable Long id) {
        return topicService.getBySubjectId(id);
    }
}