package com.fabiocondo.service.impl;

import com.fabiocondo.domain.Topic;
import com.fabiocondo.exception.domain.TopicNotFoundException;
import com.fabiocondo.repository.TopicRepository;
import com.fabiocondo.repository.filter.TopicFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

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
        topic.setTopicId(UUID.randomUUID().toString());
        return topicRepository.save(topic);
    }

    public Topic update(Topic topic, Long id) throws TopicNotFoundException {
        Topic existTopic = findById(id);
        BeanUtils.copyProperties(topic, existTopic, "id", "topicId", "isReadyForQuiz", "questions");
        logger.info("Updating topic: " + topic.getName());
        return topicRepository.save(existTopic);
    }

    public Page<Topic> filter(TopicFilter topicFilter, Pageable pageable) {
        return topicRepository.filter(topicFilter, pageable);
    }

    public Page<Topic> findAll(Pageable pageable) {
        return topicRepository.findAll(pageable);
    }

    public List<Topic> findAll() {
        return topicRepository.findAll();
    }

    public List<Topic> getBySubjectId(Long subjectId) {
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//            throw new RuntimeException("A operação foi interrompida", e);
//        }

        return topicRepository.findBySubjectIdAndIsReadyForQuizTrueOrderByNameAsc(subjectId);
        //return topicRepository.findBySubjectIdOrderByNameAsc(subjectId);
    }

    public void delete(Long id) throws TopicNotFoundException {
        Topic existTopic = findById(id);
        logger.info("Deleting topic: " + existTopic.getName());
        topicRepository.deleteById(id);
    }

    public long getTotal(){
        return topicRepository.count();
    }
}
