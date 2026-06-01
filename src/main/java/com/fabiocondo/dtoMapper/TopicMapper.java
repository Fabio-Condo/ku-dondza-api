package com.fabiocondo.dtoMapper;

import com.fabiocondo.domain.Topic;
import com.fabiocondo.domain.User;
import com.fabiocondo.dto.SubjectDto;
import com.fabiocondo.dto.TopicContentDTO;
import com.fabiocondo.dto.TopicDTO;
import com.fabiocondo.repository.UserRepository;
import com.fabiocondo.service.impl.SubjectServiceImpl;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class TopicMapper {

    private final UserRepository userRepository;
    private final SubjectServiceImpl subjectService;


    public TopicMapper(UserRepository userRepository, SubjectServiceImpl subjectService) {
        this.userRepository = userRepository;
        this.subjectService = subjectService;
    }

    public Topic dtoToDomainObject(TopicDTO topicDTO) {
        Topic topic = new Topic();
        topic.setId(topicDTO.getId());
        topic.setTopicId(topicDTO.getTopicId());
        topic.setName(topicDTO.getName());
        topic.setDescription(topicDTO.getDescription());
        //topic.setSubject(topicDTO.getSubject());
        topic.setEnabled(topicDTO.isEnabled());
        topic.setPremium(topicDTO.isPremium());
        topic.setPosition(topicDTO.getPosition());
        topic.setQuestions(topicDTO.getQuestions());
        //topic.setContents(topicDTO.getContents());
        return topic;
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
        topicDTO.setQuestions(topic.getQuestions());

        // SUBJECT
        if (topic.getSubject() != null) {
            SubjectDto subjectDTO = new SubjectDto();
            subjectDTO.setId(topic.getSubject().getId());
            subjectDTO.setSubjectId(topic.getSubject().getSubjectId());
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

    public TopicDTO domainToDTO_2(Topic topic) {
        TopicDTO topicDTO = new TopicDTO();
        topicDTO.setId(topic.getId());
        topicDTO.setTopicId(topic.getTopicId());
        topicDTO.setName(topic.getName());
        topicDTO.setDescription(topic.getDescription());
        topicDTO.setEnabled(topic.isEnabled());
        topicDTO.setPremium(topic.isPremium());
        topicDTO.setPosition(topic.getPosition());

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

    //public Page<TopicDTO> domainPageToDTOPage(Page<Topic> topics, Pageable pageable) {
    //    return new PageImpl<>(topics.stream()
    //            .map(this::domainToDTO)
    //            .collect(Collectors.toList()), pageable, topics.getTotalElements());
    //}
}
