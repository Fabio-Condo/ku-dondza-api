package com.fabiocondo.dtoMapper;

import com.fabiocondo.domain.TopicTest;
import com.fabiocondo.dto.TopicTestDTO;
import com.fabiocondo.dto.TopicWithTestsDTO;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Component
public class TopicTestMapper {

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
        //topicTestDTO.setQuestions(topicTest.getQuestions());
        //topicTestDTO.setSubmittedQuizzes(topicTest.getSubmittedQuizzes());
        topicTestDTO.setOrderIndex(topicTest.getOrderIndex());
        topicTestDTO.setTopicTestStatus(topicTest.getTopicTestStatus());
        topicTestDTO.setAccuracyRate(100.0);
        topicTestDTO.setTotalQuestions((long) topicTestDTO.getQuestions().size());

        return topicTestDTO;
    }

    public List<TopicTestDTO> toDTOListOrdered(List<TopicTest> tests) {
        return tests.stream()
                // primeiro converte para DTO
                .map(this::domainToDTO)
                // ordena por: nome do tópico e depois pelo orderIndex
                .sorted(Comparator.comparing((TopicTestDTO dto) -> dto.getTopic().getName())
                        .thenComparing(TopicTestDTO::getOrderIndex))
                .collect(Collectors.toList());
    }

    public List<TopicWithTestsDTO> groupByTopic(List<TopicTest> tests) {
        // converte para DTO
        List<TopicTestDTO> dtos = toDTOListOrdered(tests);

        // agrupa por nome do tópico
        Map<String, List<TopicTestDTO>> grouped = dtos.stream()
                .collect(Collectors.groupingBy(dto -> dto.getTopic().getName()));

        // transforma em lista de TopicWithTestsDTO
        return grouped.entrySet().stream()
                .map(entry -> new TopicWithTestsDTO(
                        entry.getValue().get(0).getTopic().getId(), // id do tópico
                        entry.getKey(),                              // nome do tópico
                        entry.getValue()                             // lista de testes do tópico
                ))
                .sorted(Comparator.comparing(TopicWithTestsDTO::getTopicName))
                .collect(Collectors.toList());
    }

}
