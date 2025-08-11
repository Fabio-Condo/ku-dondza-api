package com.fabiocondo.dtoMapper;

import com.fabiocondo.domain.Subject;
import com.fabiocondo.domain.User;
import com.fabiocondo.dto.SubjectDto;
import com.fabiocondo.repository.UserRepository;
import com.fabiocondo.service.impl.SubjectServiceImpl;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SubjectMapper {

    private final UserRepository userRepository;

    private final SubjectServiceImpl subjectService;

    public SubjectMapper(UserRepository userRepository, SubjectServiceImpl subjectService) {
        this.userRepository = userRepository;
        this.subjectService = subjectService;
    }

    public SubjectDto domainToDto(Subject subject) {
        SubjectDto subjectDto = new SubjectDto();
        subjectDto.setId(subject.getId());
        subjectDto.setSubjectId(subject.getSubjectId());
        subjectDto.setName(subject.getName());
        subjectDto.setDescription(subject.getDescription());
        return subjectDto;
    }

    public SubjectDto domainToDtoWithTopics(Subject subject, Long currentUserId) {
        SubjectDto subjectDto = new SubjectDto();
        subjectDto.setId(subject.getId());
        subjectDto.setSubjectId(subject.getSubjectId());
        subjectDto.setName(subject.getName());
        subjectDto.setDescription(subject.getDescription());
        subjectDto.setTopics(subject.getTopics());

        Optional<User> currentUser = userRepository.findById(currentUserId);

        if(currentUser.isPresent()){
            subjectDto.setCurrentUserSubscribed(subjectService.checkIfCurrentUserSubscribed(subject.getId(), currentUserId));
        }
        return subjectDto;
    }
}
