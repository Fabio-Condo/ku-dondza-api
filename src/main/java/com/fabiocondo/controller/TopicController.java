package com.fabiocondo.controller;

import com.fabiocondo.constant.CacheNames;
import com.fabiocondo.domain.HttpResponse;
import com.fabiocondo.domain.Topic;
import com.fabiocondo.dto.TopicDTO;
import com.fabiocondo.dtoMapper.TopicMapper;
import com.fabiocondo.exception.domain.TopicNotFoundException;
import com.fabiocondo.repository.filter.TopicFilter;
import com.fabiocondo.service.impl.TopicService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/topics")
public class TopicController {

    public final TopicService topicService;
    private final TopicMapper topicMapper;


    public TopicController(TopicService topicService, TopicMapper topicMapper) {
        this.topicService = topicService;
        this.topicMapper = topicMapper;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Topic> findById(@PathVariable("id") Long id) throws TopicNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(topicService.findById(id));
    }

    //@GetMapping("/find-by-topicId/{topicId}")
    //public ResponseEntity<Topic> findTopicByTopicId(@PathVariable("topicId") String topicId) throws TopicNotFoundException {
    //    Topic topic = topicService.findTopicByTopicId(topicId);
    //    return ResponseEntity.status(HttpStatus.OK).body(topic);
    //}

    @GetMapping("/find-by-topicId/{topicId}")
    public ResponseEntity<TopicDTO> findTopicByTopicId(@PathVariable("topicId") String topicId) throws TopicNotFoundException {
        Topic topic = topicService.findTopicByTopicId(topicId);
        return ResponseEntity.status(HttpStatus.OK).body(topicMapper.domainToDTO(topic));
    }

    @PostMapping
    public ResponseEntity<Topic> save(@RequestBody Topic topic) throws TopicNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(topicService.save(topic));
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

    //@GetMapping("/subjects/{subjectId}")
    public List<Topic> getBySubjectId(@PathVariable Long subjectId) {
        return topicService.getBySubjectId(subjectId);
    }

    @Cacheable(
            value = CacheNames.TOPIC_LIST,
            key = "#subjectId"
    )
    @GetMapping("/subjects/{subjectId}")
    public List<TopicDTO> getBySubjectIdWithCache(
            @PathVariable Long subjectId
    ) {
        return topicService.getBySubjectId(subjectId)
                .stream()
                .map(topicMapper::domainToDTO_2)
                .collect(Collectors.toList());
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