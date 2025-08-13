package com.fabiocondo.dtoMapper;

import com.fabiocondo.domain.Subject;
import com.fabiocondo.domain.User;
import com.fabiocondo.dto.SubjectDto;
import com.fabiocondo.exception.domain.UserNotFoundException;
import com.fabiocondo.repository.UserRepository;
import com.fabiocondo.service.impl.SubjectServiceImpl;
import com.fabiocondo.service.impl.TopicService;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SubjectMapper {

    private final UserRepository userRepository;

    private final SubjectServiceImpl subjectService;
    private final TopicService topicService;

    public SubjectMapper(UserRepository userRepository, SubjectServiceImpl subjectService, TopicService topicService) {
        this.userRepository = userRepository;
        this.subjectService = subjectService;
        this.topicService = topicService;
    }

    public SubjectDto domainToDto(Subject subject) {
        SubjectDto subjectDto = new SubjectDto();
        subjectDto.setId(subject.getId());
        subjectDto.setSubjectId(subject.getSubjectId());
        subjectDto.setName(subject.getName());
        subjectDto.setDescription(subject.getDescription());
        return subjectDto;
    }

    public SubjectDto domainToDtoWithTopics(Subject subject, Long currentUserId) throws UserNotFoundException {
        SubjectDto subjectDto = new SubjectDto();
        subjectDto.setId(subject.getId());
        subjectDto.setSubjectId(subject.getSubjectId());
        subjectDto.setName(subject.getName());
        subjectDto.setDescription(subject.getDescription());

        //subjectDto.setTopics(subject.getTopics());
        subjectDto.setTopics(topicService.getBySubjectId(subject.getId()));

        Optional<User> currentUser = userRepository.findById(currentUserId);

        if(currentUser.isPresent()){
            subjectDto.setCurrentUserSubscribed(subjectService.checkIfCurrentUserSubscribed(subject.getId(), currentUserId));
            subjectDto.setCurrentUserMarkedContentRate(subjectService.calculateUserProgressInSubject(currentUserId, subject.getId()));
        }
        return subjectDto;
    }
}
