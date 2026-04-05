package com.fabiocondo.dtoMapper;

import com.fabiocondo.domain.Subject;
import com.fabiocondo.domain.Test;
import com.fabiocondo.domain.Topic;
import com.fabiocondo.domain.User;
import com.fabiocondo.dto.SubjectDto;
import com.fabiocondo.dto.SubjectProgressDTO;
import com.fabiocondo.dto.TopicDtoWithTests;
import com.fabiocondo.exception.domain.UserNotFoundException;
import com.fabiocondo.repository.TopicRepository;
import com.fabiocondo.repository.TopicTestRepository;
import com.fabiocondo.repository.UserRepository;
import com.fabiocondo.service.impl.SubjectServiceImpl;
import com.fabiocondo.service.impl.TopicService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class SubjectMapper {

    private final UserRepository userRepository;
    private final SubjectServiceImpl subjectService;
    private final TopicRepository topicRepository;
    private final TopicService topicService;
    public final TopicTestRepository topicTestRepository;
    private final TestMapper testMapper;

    public SubjectMapper(UserRepository userRepository, SubjectServiceImpl subjectService, TopicRepository topicRepository, TopicService topicService, TopicTestRepository topicTestRepository, TestMapper testMapper) {
        this.userRepository = userRepository;
        this.subjectService = subjectService;
        this.topicRepository = topicRepository;
        this.topicService = topicService;
        this.topicTestRepository = topicTestRepository;
        this.testMapper = testMapper;
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

    public SubjectProgressDTO mapSubjectToProgressDTO(Subject subject, Long userId) {

        //List<Test> allTests = topicTestRepository.findAll();
        List<Test> allTests = topicTestRepository.findBySubjectId(subject.getId());

        SubjectProgressDTO dto = new SubjectProgressDTO();
        dto.setId(subject.getId());
        dto.setSubjectId(subject.getSubjectId());
        dto.setSubjectName(subject.getName());
        dto.setSubjectDescription(subject.getDescription());
        dto.setSubjectCategory(subject.getCategory());

        List<Topic> topics = topicRepository
                .findBySubjectIdAndEnabledTrueOrderByPositionAsc(subject.getId());

        List<TopicDtoWithTests> topicDTOs = topics.stream()
                .map(topic -> testMapper.mapToTopicTestsDTOWithTests(topic, allTests, userId))
                .collect(Collectors.toList());

        dto.setTopicDtoWithTests(topicDTOs);

        dto.setTotalTopics((long) topics.size());

        // 👇 agora consistente com allTests
        List<Test> subjectTests = allTests.stream()
                .filter(test -> test.getTopic().getSubject().getId().equals(subject.getId()))
                .collect(Collectors.toList());

        long submittedCount = subjectTests.stream()
                .filter(test -> test.getSubmittedQuizzes()
                        .stream()
                        .anyMatch(q -> q.getUser().getId().equals(userId)))
                .count();

        double progressRate = subjectTests.isEmpty()
                ? 0
                : (submittedCount * 100.0) / subjectTests.size();

        dto.setCurrentUserProgressRate(progressRate);

        return dto;
    }

    public List<SubjectProgressDTO> mapSubjectsToProgressDTOs(List<Subject> subjects, Long userId) {

        // buscar TODOS os testes uma vez (evita N+1)
        List<Test> allTests = topicTestRepository.findAll();

        return subjects.stream()
                .map(subject -> {

                    SubjectProgressDTO dto = new SubjectProgressDTO();
                    dto.setId(subject.getId());
                    dto.setSubjectId(subject.getSubjectId());
                    dto.setSubjectName(subject.getName());
                    dto.setSubjectDescription(subject.getDescription());
                    dto.setSubjectCategory(subject.getCategory());

                    // buscar tópicos da disciplina
                    List<Topic> topics = topicRepository
                            .findBySubjectIdAndEnabledTrueOrderByPositionAsc(subject.getId());

                    // limitar a 3 tópicos + reutilizar teu método
                    List<TopicDtoWithTests> topicDTOs = topics.stream()
                            .limit(3)
                            .map(topic -> testMapper.mapToTopicTestsDTO(topic, allTests, userId)).collect(Collectors.toList());

                    dto.setTopicDtoWithTests(topicDTOs);

                    dto.setTotalTopics((long) topics.size());

                    // calcular progresso TOTAL da disciplina (reutilizando lógica)
                    List<Test> subjectTests = allTests.stream()
                            .filter(test -> test.getTopic().getSubject().getId().equals(subject.getId()))
                            .collect(Collectors.toList());

                    long submittedCount = subjectTests.stream()
                            .filter(test -> test.getSubmittedQuizzes()
                                    .stream()
                                    .anyMatch(q -> q.getUser().getId().equals(userId)))
                            .count();

                    double progressRate = subjectTests.isEmpty()
                            ? 0
                            : (submittedCount * 100.0) / subjectTests.size();

                    dto.setCurrentUserProgressRate(progressRate);

                    return dto;

                })
                .collect(Collectors.toList());
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
}
