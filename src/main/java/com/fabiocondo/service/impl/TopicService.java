package com.fabiocondo.service.impl;

import com.fabiocondo.constant.CacheNames;
import com.fabiocondo.domain.Question;
import com.fabiocondo.domain.Topic;
import com.fabiocondo.dto.*;
import com.fabiocondo.exception.domain.QuestionNotFoundException;
import com.fabiocondo.exception.domain.TopicNotFoundException;
import com.fabiocondo.repository.QuestionRepository;
import com.fabiocondo.repository.TopicRepository;
import com.fabiocondo.repository.filter.TopicFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TopicService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    TopicRepository topicRepository;

    QuestionRepository questionRepository;

    public TopicService(TopicRepository topicRepository, QuestionRepository questionRepository) {
        this.topicRepository = topicRepository;
        this.questionRepository = questionRepository;
    }

    public Topic findById(Long id) throws TopicNotFoundException {
        logger.info("Getting topic by id: " + id);
        return topicRepository.findById(id)
                .orElseThrow(() -> new TopicNotFoundException("No topic found by id: " + id));
    }

    public Topic findTopicByTopicId(String topicId) throws TopicNotFoundException {
        return topicRepository.findTopicByTopicId(topicId)
                .orElseThrow(() -> new TopicNotFoundException("No topic found by id: " + topicId));
    }

    @Cacheable(
            value = CacheNames.TOPIC_DETAIL,
            key = "#topicId",
            unless = "#result == null"
    )
    public TopicDTO findTopicByTopicId_WithCache(String topicId) throws TopicNotFoundException {
        Topic topic = topicRepository.findTopicByTopicId(topicId)
                .orElseThrow(() -> new TopicNotFoundException("No topic found by id: " + topicId));

        return domainToDTO_WithContent(topic);
    }

    public Topic save(Topic topic) {
        topic.setTopicId(UUID.randomUUID().toString());
        return topicRepository.save(topic);
    }

    public Topic update(Topic topic, Long id) throws TopicNotFoundException {
        Topic existTopic = findById(id);
        BeanUtils.copyProperties(topic, existTopic, "id", "topicId", "isReadyForQuiz", "questions", "contents");
        logger.info("Updating topic: " + topic.getName());
        return topicRepository.save(existTopic);
    }

    @Cacheable(
            value = CacheNames.QUESTION_FILTER,
            key =
                    "#topicFilter.searchParam + '-' +" +
                            "#topicFilter.subjectId + '-' +" +
                            "#pageable.pageNumber + '-' +" +
                            "#pageable.pageSize + '-' +" +
                            "#pageable.sort.toString()"
    )
    public PageResponse<TopicDTO> filterWithCash(TopicFilter topicFilter, Pageable pageable) {

        Page<Topic> page = topicRepository.filter(topicFilter, pageable);

        List<TopicDTO> content =
                page.getContent()
                        .stream()
                        .map(t -> domainToDTO(t))
                        .collect(Collectors.toList());

        return new PageResponse<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements()
        );
    }

    public Page<Topic> findAll(Pageable pageable) {
        return topicRepository.findAll(pageable);
    }

    public List<Topic> findAll() {
        return topicRepository.findAll();
    }


    public List<Topic> getBySubjectId(Long subjectId) {
        return topicRepository.findBySubjectIdAndEnabledTrueOrderByPositionAsc(subjectId);
    }

    public void delete(Long id) throws TopicNotFoundException {
        Topic existTopic = findById(id);
        logger.info("Deleting topic: " + existTopic.getName());
        topicRepository.deleteById(id);
    }

    public long getTotal(){
        return topicRepository.count();
    }

    public TopicDTO domainToDTO_WithContent(Topic topic) {

        TopicDTO topicDTO = new TopicDTO();

        topicDTO.setId(topic.getId());
        topicDTO.setTopicId(topic.getTopicId());
        topicDTO.setName(topic.getName());
        topicDTO.setDescription(topic.getDescription());
        topicDTO.setEnabled(topic.isEnabled());
        topicDTO.setPremium(topic.isPremium());
        topicDTO.setPosition(topic.getPosition());
        topicDTO.setTotalQuestions(questionRepository.countByTopicId(topic.getId()));

        // SUBJECT
        if (topic.getSubject() != null) {
            SubjectDto subjectDTO = new SubjectDto();
            subjectDTO.setId(topic.getSubject().getId());
            subjectDTO.setSubjectId(topic.getSubject().getSubjectId());
            subjectDTO.setCategory(topic.getSubject().getCategory());
            subjectDTO.setName(topic.getSubject().getName());
            subjectDTO.setDescription(topic.getSubject().getDescription());
            topicDTO.setSubject(subjectDTO);
        }

        // CONTENTS
        if (topic.getContents() != null) {

            List<TopicContentDTO> contentDTOList = topic.getContents()
                    .stream()
                    .map(content -> {

                        TopicContentDTO dto = new TopicContentDTO();

                        dto.setId(content.getId());
                        dto.setDescription(content.getDescription());
                        dto.setContentType(content.getContentType());
                        dto.setTime(content.getTime());
                        dto.setFileName(content.getFileName());
                        dto.setUrlFile(content.getUrlFile());
                        dto.setPosition(content.getPosition());
                        dto.setMarkedByUser(content.isMarkedByUser());

                        return dto;
                    })
                    .collect(Collectors.toList());

            topicDTO.setContents(contentDTOList);
        }

        return topicDTO;
    }

    public TopicDTO domainToDTO(Topic topic) {
        TopicDTO topicDTO = new TopicDTO();
        topicDTO.setId(topic.getId());
        topicDTO.setTopicId(topic.getTopicId());
        topicDTO.setName(topic.getName());
        topicDTO.setDescription(topic.getDescription());
        topicDTO.setEnabled(topic.isEnabled());
        topicDTO.setPremium(topic.isPremium());
        topicDTO.setPosition(topic.getPosition());
        topicDTO.setTotalQuestions(questionRepository.countByTopicId(topic.getId()));

        if (topic.getSubject() != null) {
            SubjectDto subjectDTO = new SubjectDto();
            subjectDTO.setId(topic.getSubject().getId());
            subjectDTO.setSubjectId(topic.getSubject().getSubjectId());
            subjectDTO.setName(topic.getSubject().getName());
            subjectDTO.setDescription(topic.getSubject().getDescription());
            topicDTO.setSubject(subjectDTO);
        }

        return topicDTO;
    }

}
