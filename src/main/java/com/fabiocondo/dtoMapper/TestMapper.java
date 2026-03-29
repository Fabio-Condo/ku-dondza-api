package com.fabiocondo.dtoMapper;

import com.fabiocondo.domain.Quiz;
import com.fabiocondo.domain.Test;
import com.fabiocondo.domain.Topic;
import com.fabiocondo.dto.TestDTO;
import com.fabiocondo.dto.TopicDtoWithTests;
import com.fabiocondo.repository.TopicTestRepository;
import com.fabiocondo.repository.UserRepository;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;


@Component
public class TestMapper {

    TopicTestRepository topicTestRepository;
    UserRepository userRepository;

    public TestMapper(TopicTestRepository topicTestRepository) {
        this.topicTestRepository = topicTestRepository;
    }

    public Test dtoToDomainObject(TestDTO testDTO) {
        Test test = new Test();
        test.setId(testDTO.getId());
        test.setDifficultyLevel(testDTO.getDifficultyLevel());
        test.setTopic(testDTO.getTopic());
        test.setQuestions(testDTO.getQuestions());
        test.setSubmittedQuizzes(testDTO.getSubmittedQuizzes());
        test.setOrderIndex(testDTO.getOrderIndex());
        return test;
    }

    public TestDTO domainToDTO(Test test) {
        TestDTO testDTO = new TestDTO();
        testDTO.setId(test.getId());
        testDTO.setDifficultyLevel(test.getDifficultyLevel());
        testDTO.setTopic(test.getTopic());
        //testDTO.setSubmittedQuizzes(test.getSubmittedQuizzes());
        testDTO.setOrderIndex(test.getOrderIndex());
        testDTO.setAccuracyRate(100.0);
        testDTO.setTotalQuestions((long) testDTO.getQuestions().size());

        return testDTO;
    }

    TopicDtoWithTests mapToTopicTestsDTO(Topic topic, List<Test> allTests, Long userId) {

        TopicDtoWithTests dto = new TopicDtoWithTests();

        dto.setTopicId(topic.getId());
        dto.setTopicName(topic.getName());

        // 👇 filtrar testes do tópico
        List<Test> topicTests = allTests.stream()
                .filter(test -> test.getTopic().getId().equals(topic.getId()))
                .sorted(Comparator.comparingInt(Test::getOrderIndex)).collect(Collectors.toList());

        // 👇 calcular progresso
        long submittedCount = topicTests.stream()
                .filter(test -> test.getSubmittedQuizzes()
                        .stream()
                        .anyMatch(q -> q.getUser().getId().equals(userId)))
                .count();

        double progressRate = topicTests.isEmpty()
                ? 0
                : (submittedCount * 100.0) / topicTests.size();

        dto.setProgressRate(progressRate);

        dto.setCompleted(submittedCount == topicTests.size());

        return dto;
    }

    TopicDtoWithTests mapToTopicTestsDTOWithTests(Topic topic, List<Test> allTests, Long userId) {

        TopicDtoWithTests dto = new TopicDtoWithTests();

        dto.setTopicId(topic.getId());
        dto.setTopicName(topic.getName());

        // filtrar testes do tópico
        List<Test> topicTests = allTests.stream()
                .filter(test -> test.getTopic().getId().equals(topic.getId()))
                .sorted(Comparator.comparingInt(Test::getOrderIndex))
                .collect(Collectors.toList());

        // converter para DTO completo
        List<TestDTO> testDTOs = topicTests.stream()
                .map(test -> {
                    TestDTO t = new TestDTO();
                    t.setId(test.getId());
                    t.setDifficultyLevel(test.getDifficultyLevel());
                    t.setOrderIndex(test.getOrderIndex());
                    t.setTotalQuestions((long) test.getQuestions().size());

                    // buscar APENAS o quiz do user atual
                    Optional<Quiz> userQuiz =
                            topicTestRepository.findUserQuizByTopicTest(test.getId(), userId);

                    userQuiz.ifPresent(quiz -> {
                        Set<Quiz> quizzes = new HashSet<>();
                        quizzes.add(quiz);
                        t.setSubmittedQuizzes(quizzes);
                    });

                    //t.setSubmittedQuizzes(test.getSubmittedQuizzes());
                    //t.setAccuracyRate(accuracyRate);
                    return t;
                })
                .collect(Collectors.toList());

        dto.setTests(testDTOs);

        // progresso do tópico
        long submittedCount = topicTests.stream()
                .filter(test -> test.getSubmittedQuizzes()
                        .stream()
                        .anyMatch(q -> q.getUser().getId().equals(userId)))
                .count();

        double progressRate = topicTests.isEmpty()
                ? 0
                : (submittedCount * 100.0) / topicTests.size();

        dto.setProgressRate(progressRate);
        dto.setCompleted(submittedCount == topicTests.size());

        return dto;
    }

    public TestDTO domainToDTO(Test test, Long userId) {
        TestDTO dto = new TestDTO();

        dto.setId(test.getId());
        dto.setDifficultyLevel(test.getDifficultyLevel());
        dto.setTopic(test.getTopic());
        dto.setOrderIndex(test.getOrderIndex());
        dto.setAccuracyRate(100.0);
        dto.setTotalQuestions((long) test.getQuestions().size());

        // buscar APENAS o quiz do user atual
        Optional<Quiz> userQuiz =
                topicTestRepository.findUserQuizByTopicTest(test.getId(), userId);

        userQuiz.ifPresent(quiz -> {
            Set<Quiz> quizzes = new HashSet<>();
            quizzes.add(quiz);
            dto.setSubmittedQuizzes(quizzes);
        });

        return dto;
    }

    public List<TestDTO> toDTOListOrdered(List<Test> tests, Long user) {
        return tests.stream()
                .map(test -> domainToDTO(test, user))
                .sorted(
                        Comparator
                                .comparing((TestDTO dto) -> dto.getTopic().getName())
                                .thenComparing(TestDTO::getOrderIndex)
                )
                .collect(Collectors.toList());
    }

    public List<TopicDtoWithTests> groupByTopic(List<Test> tests, Long userId) {

        List<TestDTO> dtos = toDTOListOrdered(tests, userId);

        Map<String, List<TestDTO>> grouped =
                dtos.stream()
                        .collect(Collectors.groupingBy(dto -> dto.getTopic().getName()));

        return grouped.entrySet().stream()
                .map(entry -> new TopicDtoWithTests(
                        entry.getValue().get(0).getTopic().getId(),
                        entry.getKey(),
                        entry.getValue()
                ))
                .sorted(Comparator.comparing(TopicDtoWithTests::getTopicName))
                .collect(Collectors.toList());
    }

}
