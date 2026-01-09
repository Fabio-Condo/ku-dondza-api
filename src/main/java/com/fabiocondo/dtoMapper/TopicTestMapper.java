package com.fabiocondo.dtoMapper;

import com.fabiocondo.domain.Quiz;
import com.fabiocondo.domain.TopicTest;
import com.fabiocondo.dto.TopicTestDTO;
import com.fabiocondo.dto.TopicWithTestsDTO;
import com.fabiocondo.repository.TopicTestRepository;
import com.fabiocondo.repository.UserRepository;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;


@Component
public class TopicTestMapper {

    TopicTestRepository topicTestRepository;
    UserRepository userRepository;

    public TopicTestMapper(TopicTestRepository topicTestRepository) {
        this.topicTestRepository = topicTestRepository;
    }

    public TopicTest dtoToDomainObject(TopicTestDTO topicTestDTO) {
        TopicTest topicTest = new TopicTest();
        topicTest.setId(topicTestDTO.getId());
        topicTest.setDifficultyLevel(topicTestDTO.getDifficultyLevel());
        topicTest.setTopic(topicTestDTO.getTopic());
        topicTest.setQuestions(topicTestDTO.getQuestions());
        topicTest.setSubmittedQuizzes(topicTestDTO.getSubmittedQuizzes());
        topicTest.setOrderIndex(topicTestDTO.getOrderIndex());
        return topicTest;
    }

    public TopicTestDTO domainToDTO(TopicTest topicTest) {
        TopicTestDTO topicTestDTO = new TopicTestDTO();
        topicTestDTO.setId(topicTest.getId());
        topicTestDTO.setDifficultyLevel(topicTest.getDifficultyLevel());
        topicTestDTO.setTopic(topicTest.getTopic());
        //topicTestDTO.setSubmittedQuizzes(topicTest.getSubmittedQuizzes());
        topicTestDTO.setOrderIndex(topicTest.getOrderIndex());
        topicTestDTO.setAccuracyRate(100.0);
        topicTestDTO.setTotalQuestions((long) topicTestDTO.getQuestions().size());

        return topicTestDTO;
    }

    public TopicTestDTO domainToDTO(TopicTest topicTest, Long userId) {
        TopicTestDTO dto = new TopicTestDTO();

        dto.setId(topicTest.getId());
        dto.setDifficultyLevel(topicTest.getDifficultyLevel());
        dto.setTopic(topicTest.getTopic());
        dto.setOrderIndex(topicTest.getOrderIndex());
        dto.setAccuracyRate(100.0);
        dto.setTotalQuestions((long) topicTest.getQuestions().size());

        // buscar APENAS o quiz do user atual
        Optional<Quiz> userQuiz =
                topicTestRepository.findUserQuizByTopicTest(topicTest.getId(), userId);

        userQuiz.ifPresent(quiz -> {
            Set<Quiz> quizzes = new HashSet<>();
            quizzes.add(quiz);
            dto.setSubmittedQuizzes(quizzes);
        });

        return dto;
    }

    public List<TopicTestDTO> toDTOListOrdered(List<TopicTest> tests, Long user) {
        return tests.stream()
                .map(test -> domainToDTO(test, user))
                .sorted(
                        Comparator
                                .comparing((TopicTestDTO dto) -> dto.getTopic().getName())
                                .thenComparing(TopicTestDTO::getOrderIndex)
                )
                .collect(Collectors.toList());
    }

    public List<TopicWithTestsDTO> groupByTopic(List<TopicTest> tests, Long userId) {

        List<TopicTestDTO> dtos = toDTOListOrdered(tests, userId);

        Map<String, List<TopicTestDTO>> grouped =
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
