package com.fabiocondo.service.impl;

import com.fabiocondo.domain.FlashCard;
import com.fabiocondo.domain.Topic;
import com.fabiocondo.dto.*;
import com.fabiocondo.exception.domain.QuestionNotFoundException;
import com.fabiocondo.repository.FlashCardRepository;
import com.fabiocondo.repository.filter.FlashCardFilter;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class FlashCardServiceImpl {

    private final FlashCardRepository flashCardRepository;

    public FlashCardServiceImpl(FlashCardRepository flashCardRepository) {
        this.flashCardRepository = flashCardRepository;
    }

    public FlashCard findById(Long id) throws QuestionNotFoundException {
        return flashCardRepository.findById(id)
                .orElseThrow(() -> new QuestionNotFoundException("No flash card found by id: " + id));
    }

    public FlashCard save(FlashCard flashCard) {
        return flashCardRepository.save(flashCard);
    }

    public FlashCard update(FlashCard flashCard, Long id) throws QuestionNotFoundException {
        FlashCard existFlashCard = findById(id);
        //existFlashCard.setUpdatedAt(new Date());
        BeanUtils.copyProperties(flashCard, existFlashCard, "createdAt");
        return flashCardRepository.save(existFlashCard);
    }

    public PageResponse<FlashCardResponse> filter(FlashCardFilter flashCardFilter, Pageable pageable) {

        Page<FlashCard> page = flashCardRepository.filter(flashCardFilter, pageable);

        List<FlashCardResponse> content =
                page.getContent()
                        .stream()
                        .map(fc -> mapToResponse(fc))
                        .collect(Collectors.toList());

        return new PageResponse<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements()
        );
    }

    public FlashCardDeckResponse getDeck(Long topicId) {

        List<FlashCard> cards = flashCardRepository.findByTopic_Id(topicId);

        List<FlashCardResponse> responses =
                cards.stream()
                        .map(card -> mapToResponse(card))
                        .collect(Collectors.toList());

        FlashCardDeckResponse response = new FlashCardDeckResponse();

        response.setTopicId(topicId);
        response.setCards(responses);

        if (!cards.isEmpty()) {

            Topic topic = cards.get(0).getTopic();

            response.setTopicName(topic.getName());

            if (topic.getSubject() != null) {
                response.setSubjectId(topic.getSubject().getSubjectId());
                response.setSubjectName(topic.getSubject().getName());
            }
        }

        return response;
    }

    private FlashCardResponse mapToResponse(FlashCard card) {

        FlashCardResponse response = new FlashCardResponse();

        response.setId(card.getId());
        response.setQuestion(card.getQuestion());
        response.setAnswer(card.getAnswer());
        response.setCategory(card.getCategory());
        response.setNote(card.getNote());

        if (card.getTopic() != null) {
            TopicDTO topicDTO = new TopicDTO();
            topicDTO.setId(card.getTopic().getId());
            topicDTO.setName(card.getTopic().getName());

            // CONVERTER Subject para SubjectDTO
            if (card.getTopic().getSubject() != null) {
                SubjectDto subjectDTO = new SubjectDto();
                subjectDTO.setId(card.getTopic().getSubject().getId());
                subjectDTO.setName(card.getTopic().getSubject().getName());
                subjectDTO.setDescription(card.getTopic().getSubject().getDescription());
                subjectDTO.setCategory(card.getTopic().getSubject().getCategory());
                topicDTO.setSubject(subjectDTO);
            }

            topicDTO.setDescription(card.getTopic().getDescription());
            topicDTO.setPosition(card.getTopic().getPosition());
            response.setTopic(topicDTO);
        }

        return response;
    }

}
