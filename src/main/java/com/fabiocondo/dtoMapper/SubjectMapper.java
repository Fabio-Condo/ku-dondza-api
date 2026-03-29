package com.fabiocondo.dtoMapper;

import com.fabiocondo.domain.Subject;
import com.fabiocondo.domain.Test;
import com.fabiocondo.domain.User;
import com.fabiocondo.dto.SubjectDto;
import com.fabiocondo.dto.SubjectProgressDTO;
import com.fabiocondo.dto.TestDTO;
import com.fabiocondo.exception.domain.UserNotFoundException;
import com.fabiocondo.repository.TopicRepository;
import com.fabiocondo.repository.UserRepository;
import com.fabiocondo.service.impl.SubjectServiceImpl;
import com.fabiocondo.service.impl.TopicService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class SubjectMapper {

    private final UserRepository userRepository;
    private final SubjectServiceImpl subjectService;
    private final TopicRepository topicRepository;
    private final TopicService topicService;

    public SubjectMapper(UserRepository userRepository, SubjectServiceImpl subjectService, TopicRepository topicRepository, TopicService topicService) {
        this.userRepository = userRepository;
        this.subjectService = subjectService;
        this.topicRepository = topicRepository;
        this.topicService = topicService;
    }

    public SubjectProgressDTO domainToDtoAAAAA(Subject subject, Long currentUserId) {
        SubjectProgressDTO subjectDto = new SubjectProgressDTO();
        subjectDto.setId(subject.getId());
        subjectDto.setSubjectId(subject.getSubjectId());
        subjectDto.setName(subject.getName());
        subjectDto.setDescription(subject.getDescription());
        subjectDto.setCategory(subject.getCategory());
        subjectDto.setTotalTopics(topicRepository.countBySubjectIdAndEnabledTrue(subject.getId()));

        Optional<User> currentUser = userRepository.findById(currentUserId);

        if(currentUser.isPresent()){
            subjectDto.setCurrentUserProgressRate(10.0);
        }
        return subjectDto;
    }

    public SubjectDto domainToDto(Subject subject, Long currentUserId) throws UserNotFoundException {
        SubjectDto subjectDto = new SubjectDto();
        subjectDto.setId(subject.getId());
        subjectDto.setSubjectId(subject.getSubjectId());
        subjectDto.setName(subject.getName());
        subjectDto.setDescription(subject.getDescription());
        subjectDto.setCategory(subject.getCategory());
        subjectDto.setTotalTopics(topicRepository.countBySubjectIdAndEnabledTrue(subject.getId()));

        Optional<User> currentUser = userRepository.findById(currentUserId);

        if(currentUser.isPresent()){
            subjectDto.setCurrentUserMarkedContentRate(subjectService.calculateUserProgressInSubject(currentUserId, subject.getId()));
        }
        return subjectDto;
    }

    public SubjectDto domainToDtoWithTopics(Subject subject, Long currentUserId) throws UserNotFoundException {
        SubjectDto subjectDto = new SubjectDto();
        subjectDto.setId(subject.getId());
        subjectDto.setSubjectId(subject.getSubjectId());
        subjectDto.setName(subject.getName());
        subjectDto.setDescription(subject.getDescription());
        subjectDto.setCategory(subject.getCategory());

        //subjectDto.setTopics(subject.getTopics());
        subjectDto.setTopics(topicService.getBySubjectId(subject.getId()));
        subjectDto.setTotalTopics(topicRepository.countBySubjectIdAndEnabledTrue(subject.getId()));

        Optional<User> currentUser = userRepository.findById(currentUserId);

        if(currentUser.isPresent()){
            subjectDto.setCurrentUserMarkedContentRate(subjectService.calculateUserProgressInSubject(currentUserId, subject.getId()));
        }
        return subjectDto;
    }

    public Page<SubjectDto> domainPageToDTOPage(Page<Subject> subjects, Long currentUserId, Pageable pageable) {
        return new PageImpl<>(
                subjects.stream()
                        .map(subject -> {
                            try {
                                return domainToDto(subject, currentUserId);
                            } catch (UserNotFoundException e) {
                                throw new RuntimeException(e);
                            }
                        })
                        .collect(Collectors.toList()),
                pageable,
                subjects.getTotalElements()
        );
    }

    public List<SubjectProgressDTO> toDTOListOrdered(List<Subject> subjects, Long user) {
        return subjects.stream()
                .map(subject -> domainToDtoAAAAA(subject, user))
                .collect(Collectors.toList());
    }
}
