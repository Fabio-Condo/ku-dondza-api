package com.fabiocondo.service.impl;

import com.fabiocondo.domain.Course;
import com.fabiocondo.domain.Topic;
import com.fabiocondo.exception.domain.TopicNotFoundException;
import com.fabiocondo.repository.TopicRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TopicService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    TopicRepository topicRepository;

    public TopicService(TopicRepository topicRepository) {
        this.topicRepository = topicRepository;
    }

    public Topic findById(Long id) throws TopicNotFoundException {
        logger.info("Getting topic by id: " + id);
        return topicRepository.findById(id)
                .orElseThrow(() -> new TopicNotFoundException("No topic found by id: " + id));
    }

    public Topic save(Topic topic) {
        return topicRepository.save(topic);
    }

    public List<Topic> findAll() {
        return topicRepository.findAll();
    }

    public List<Topic> getBySubjectId(Long subjectId) {
        return topicRepository.findBySubjectIdOrderByNameAsc(subjectId);
    }
}
