package com.fabiocondo.dtoMapper;

import com.fabiocondo.domain.Quiz;
import com.fabiocondo.domain.Test;
import com.fabiocondo.dto.TestDTO;
import com.fabiocondo.dto.TopicWithTestsDTO;
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

    public List<TopicWithTestsDTO> groupByTopic(List<Test> tests, Long userId) {

        List<TestDTO> dtos = toDTOListOrdered(tests, userId);

        Map<String, List<TestDTO>> grouped =
                dtos.stream()
                        .collect(Collectors.groupingBy(dto -> dto.getTopic().getName()));

        return grouped.entrySet().stream()
                .map(entry -> new TopicWithTestsDTO(
                        entry.getValue().get(0).getTopic().getId(),
                        entry.getKey(),
                        entry.getValue()
                ))
                .sorted(Comparator.comparing(TopicWithTestsDTO::getTopicName))
                .collect(Collectors.toList());
    }


}
