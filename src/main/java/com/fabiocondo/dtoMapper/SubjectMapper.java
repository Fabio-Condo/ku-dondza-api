package com.fabiocondo.dtoMapper;

import com.fabiocondo.domain.*;
import com.fabiocondo.dto.SubjectDto;
import com.fabiocondo.dto.SubjectProgressDTO;
import com.fabiocondo.dto.TestDTO;
import com.fabiocondo.dto.TopicDtoWithTests;
import com.fabiocondo.exception.domain.UserNotFoundException;
import com.fabiocondo.repository.TopicRepository;
import com.fabiocondo.repository.TopicTestRepository;
import com.fabiocondo.repository.UserRepository;
import com.fabiocondo.service.impl.SubjectServiceImpl;
import com.fabiocondo.service.impl.TopicService;
import com.fabiocondo.service.impl.UserSubjectScoreService;
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

    private final UserSubjectScoreService userSubjectScoreService;

    private final TestMapper testMapper;

    public SubjectMapper(UserRepository userRepository, SubjectServiceImpl subjectService, TopicRepository topicRepository, TopicService topicService, TopicTestRepository topicTestRepository, UserSubjectScoreService userSubjectScoreService, TestMapper testMapper) {
        this.userRepository = userRepository;
        this.subjectService = subjectService;
        this.topicRepository = topicRepository;
        this.topicService = topicService;
        this.topicTestRepository = topicTestRepository;
        this.userSubjectScoreService = userSubjectScoreService;
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

        subjectDto.setExamEnabled(subject.isExamEnabled());
        subjectDto.setCourseEnabled(subject.isCourseEnabled());
        subjectDto.setProgressEnabled(subject.isProgressEnabled());
        subjectDto.setQuizEnabled(subject.isQuizEnabled());

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

        subjectDto.setExamEnabled(subject.isExamEnabled());
        subjectDto.setCourseEnabled(subject.isCourseEnabled());
        subjectDto.setProgressEnabled(subject.isProgressEnabled());
        subjectDto.setQuizEnabled(subject.isQuizEnabled());

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

        SubjectProgressDTO dto = new SubjectProgressDTO();

        dto.setId(subject.getId());
        dto.setSubjectId(subject.getSubjectId());
        dto.setSubjectName(subject.getName());
        dto.setSubjectDescription(subject.getDescription());
        dto.setSubjectCategory(subject.getCategory());
        dto.setProgressEnabled(subject.isProgressEnabled());

        User user = new User();
        user.setId(userId);

        // SCORE + RANK
        dto.setCurrentUserScore(
                userSubjectScoreService.getScore(user, subject)
        );

        dto.setCurrentUserRank(
                userSubjectScoreService.getUserRank(user, subject)
        );

        // TOPICS
        List<Topic> topics = topicRepository
                .findBySubjectIdAndEnabledTrueOrderByPositionAsc(subject.getId());

        dto.setTotalTopics((long) topics.size());

        // TESTS (única fonte)
        List<Test> testsBySubject = topicTestRepository
                .findBySubjectId(subject.getId());

        int totalTests = testsBySubject.size();

        // COMPLETED TESTS (SQL optimized)
        List<Long> completedTestIdsList =
                topicTestRepository.findCompletedTestIds(userId, subject.getId());

        Set<Long> completedTestIds = new HashSet<>(completedTestIdsList);

        long submittedCount = completedTestIds.size();

        dto.setCurrentUserProgressRate(
                totalTests == 0 ? 0 : (submittedCount * 100.0) / totalTests
        );

        // GROUP BY TOPIC (usa mesma lista única)
        Map<Long, List<Test>> testsByTopic = new HashMap<>();

        for (Test test : testsBySubject) {
            Long topicId = test.getTopic().getId();

            testsByTopic
                    .computeIfAbsent(topicId, k -> new ArrayList<>())
                    .add(test);
        }

        // MAP TOPICS
        List<TopicDtoWithTests> topicDTOs = new ArrayList<>(topics.size());

        for (Topic topic : topics) {

            List<Test> topicTests = testsByTopic
                    .getOrDefault(topic.getId(), Collections.emptyList());

            TopicDtoWithTests tDto = new TopicDtoWithTests();
            tDto.setTopicId(topic.getId());
            tDto.setTopicName(topic.getName());
            tDto.setPremium(topic.isPremium());

            int topicSize = topicTests.size();
            long topicCompleted = 0;

            List<TestDTO> testDTOs = new ArrayList<>(topicSize);

            for (Test test : topicTests) {

                boolean completed = completedTestIds.contains(test.getId());

                if (completed) topicCompleted++;

                TestDTO t = new TestDTO();
                t.setId(test.getId());
                t.setDifficultyLevel(test.getDifficultyLevel());
                t.setOrderIndex(test.getOrderIndex());

                // -------------------------
                // FIX PROBLEMA 2: questions.size()
                // -------------------------
                long questionCount = test.getQuestions() != null
                        ? test.getQuestions().size()
                        : 0;

                t.setTotalQuestions(questionCount);

                // USER QUIZ (mantido como está)
                Optional<Quiz> optionalQuiz =
                        topicTestRepository.findUserQuizByTest(test.getId(), userId);

                if (optionalQuiz.isPresent()) {
                    Set<Quiz> set = new HashSet<>();
                    set.add(optionalQuiz.get());
                    t.setSubmittedQuizzes(set);
                }

                testDTOs.add(t);
            }

            tDto.setTests(testDTOs);

            tDto.setProgressRate(
                    topicSize == 0 ? 0 : (topicCompleted * 100.0) / topicSize
            );

            tDto.setCompleted(topicCompleted == topicSize);

            topicDTOs.add(tDto);
        }

        dto.setTopicDtoWithTests(topicDTOs);

        return dto;
    }

    public List<SubjectProgressDTO> mapSubjectsToProgressDTOs(List<Subject> subjects, Long userId) {

        User user = new User();
        user.setId(userId);

        return subjects.stream()
                .map(subject -> {

                    SubjectProgressDTO dto = new SubjectProgressDTO();

                    dto.setId(subject.getId());
                    dto.setSubjectId(subject.getSubjectId());
                    dto.setSubjectName(subject.getName());
                    dto.setSubjectDescription(subject.getDescription());
                    dto.setSubjectCategory(subject.getCategory());
                    dto.setProgressEnabled(subject.isProgressEnabled());

                    // trazer tudo em batch já filtrado (evita chamadas repetidas)
                    List<Topic> topics = topicRepository
                            .findBySubjectIdAndEnabledTrueOrderByPositionAsc(subject.getId());

                    List<Test> subjectTests = topicTestRepository
                            .findByTopicSubjectId(subject.getId());

                    int totalTests = subjectTests.size();

                    // otimização: evitar stream aninhado pesado
                    Set<Long> userSubmittedTestIds = subjectTests.stream()
                            .filter(t -> t.getSubmittedQuizzes() != null)
                            .filter(t -> t.getSubmittedQuizzes()
                                    .stream()
                                    .anyMatch(q -> q.getUser().getId().equals(userId)))
                            .map(Test::getId)
                            .collect(Collectors.toSet());

                    long submittedCount = userSubmittedTestIds.size();

                    // reduzir chamadas repetidas ao service (cache local por subject)
                    dto.setCurrentUserScore(
                            userSubjectScoreService.getScore(user, subject)
                    );

                    dto.setCurrentUserRank(
                            userSubjectScoreService.getUserRank(user, subject)
                    );

                    dto.setTotalTopics((long) topics.size());

                    dto.setTopicDtoWithTests(
                            topics.stream()
                                    .limit(4)
                                    .map(topic -> testMapper.mapToTopicTestsDTO(topic, subjectTests, userId))
                                    .collect(Collectors.toList())
                    );

                    dto.setCurrentUserProgressRate(
                            totalTests == 0 ? 0 : (submittedCount * 100.0) / totalTests
                    );

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
