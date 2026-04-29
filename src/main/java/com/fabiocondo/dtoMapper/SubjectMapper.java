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
        subjectDto.setFileName(subject.getFileName());
        subjectDto.setUrlFile(subject.getUrlFile());
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
        subjectDto.setFileName(subject.getFileName());
        subjectDto.setUrlFile(subject.getUrlFile());

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
        dto.setCurrentUserScore(userSubjectScoreService.getScore(user, subject));
        dto.setCurrentUserRank(userSubjectScoreService.getUserRank(user, subject));

        // TOPICS
        List<Topic> topics = topicRepository
                .findBySubjectIdAndEnabledTrueOrderByPositionAsc(subject.getId());

        dto.setTotalTopics((long) topics.size());

        // TESTS
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

        // -----------------------------
        // QUESTIONS COUNT (BATCH)
        // -----------------------------
        List<Long> testIds = new ArrayList<>(testsBySubject.size());
        for (Test t : testsBySubject) {
            testIds.add(t.getId());
        }

        Map<Long, Long> questionsCountMap = new HashMap<>();

        if (!testIds.isEmpty()) {
            List<Object[]> results =
                    topicTestRepository.countQuestionsByTestIds(testIds);

            for (Object[] row : results) {
                Long testId = ((Number) row[0]).longValue();
                Long count = ((Number) row[1]).longValue();
                questionsCountMap.put(testId, count);
            }
        }

        // -----------------------------
        // QUIZZES (BATCH - FIX N+1)
        // -----------------------------
        Map<Long, Quiz> quizByTestId = new HashMap<>();

        List<Object[]> quizResults =
                topicTestRepository.findUserQuizzesBySubjectGrouped(subject.getId(), userId);

        for (Object[] row : quizResults) {
            Long testId = ((Number) row[0]).longValue();
            Quiz quiz = (Quiz) row[1];
            quizByTestId.put(testId, quiz);
        }

        // -----------------------------
        // GROUP BY TOPIC
        // -----------------------------
        Map<Long, List<Test>> testsByTopic = new HashMap<>();

        for (Test test : testsBySubject) {
            Long topicId = test.getTopic().getId();

            testsByTopic
                    .computeIfAbsent(topicId, k -> new ArrayList<>())
                    .add(test);
        }

        // -----------------------------
        // MAP TOPICS
        // -----------------------------
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

                // QUESTIONS (MAP - sem query)
                Long questionCount = questionsCountMap.get(test.getId());
                t.setTotalQuestions(questionCount != null ? questionCount : 0);

                // QUIZ (MAP - sem query)
                Quiz userQuiz = quizByTestId.get(test.getId());

                if (userQuiz != null) {
                    Set<Quiz> set = new HashSet<>();
                    set.add(userQuiz);
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

        List<SubjectProgressDTO> result = new ArrayList<>(subjects.size());

        for (Subject subject : subjects) {

            SubjectProgressDTO dto = new SubjectProgressDTO();

            dto.setId(subject.getId());
            dto.setSubjectId(subject.getSubjectId());
            dto.setSubjectName(subject.getName());
            dto.setSubjectDescription(subject.getDescription());
            dto.setSubjectCategory(subject.getCategory());
            dto.setProgressEnabled(subject.isProgressEnabled());

            // -------------------------
            // LOAD DATA
            // -------------------------
            List<Topic> topics = topicRepository
                    .findBySubjectIdAndEnabledTrueOrderByPositionAsc(subject.getId());

            List<Test> subjectTests = topicTestRepository
                    .findByTopicSubjectId(subject.getId());

            dto.setTotalTopics((long) topics.size());

            // -------------------------
            // USER COMPLETED TESTS (1 QUERY ONLY)
            // -------------------------
            Set<Long> submittedTestIds = new HashSet<>(
                    topicTestRepository.findCompletedTestIds(userId, subject.getId())
            );

            int totalTests = subjectTests.size();
            long submittedCount = submittedTestIds.size();

            // -------------------------
            // SCORE + RANK
            // -------------------------
            dto.setCurrentUserScore(
                    userSubjectScoreService.getScore(user, subject)
            );

            dto.setCurrentUserRank(
                    userSubjectScoreService.getUserRank(user, subject)
            );

            dto.setCurrentUserProgressRate(
                    totalTests == 0 ? 0 : (submittedCount * 100.0) / totalTests
            );

            // -------------------------
            // GROUP TESTS BY TOPIC
            // -------------------------
            Map<Long, List<Test>> testsByTopic = new HashMap<>();

            for (Test test : subjectTests) {

                Long topicId = test.getTopic().getId();

                List<Test> list = testsByTopic.get(topicId);

                if (list == null) {
                    list = new ArrayList<>();
                    testsByTopic.put(topicId, list);
                }

                list.add(test);
            }

            // -------------------------
            // MAP TOPICS (LIMIT 3)
            // -------------------------
            int maxTopics = Math.min(3, topics.size());

            List<TopicDtoWithTests> topicDTOs = new ArrayList<>(maxTopics);

            for (int i = 0; i < maxTopics; i++) {

                Topic topic = topics.get(i);

                List<Test> topicTests = testsByTopic.get(topic.getId());

                if (topicTests == null) {
                    topicTests = Collections.emptyList();
                }

                int topicSize = topicTests.size();
                long topicCompleted = 0;

                for (Test test : topicTests) {
                    if (submittedTestIds.contains(test.getId())) {
                        topicCompleted++;
                    }
                }

                TopicDtoWithTests tDto = new TopicDtoWithTests();
                tDto.setTopicId(topic.getId());
                tDto.setTopicName(topic.getName());
                tDto.setPremium(topic.isPremium());

                tDto.setProgressRate(
                        topicSize == 0 ? 0 : (topicCompleted * 100.0) / topicSize
                );

                tDto.setCompleted(topicSize > 0 && topicCompleted == topicSize);

                topicDTOs.add(tDto);
            }

            dto.setTopicDtoWithTests(topicDTOs);

            result.add(dto);
        }

        return result;
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
