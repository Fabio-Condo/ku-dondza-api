package com.fabiocondo.service.impl;

import com.fabiocondo.domain.FlashCard;
import com.fabiocondo.domain.Topic;
import com.fabiocondo.domain.UserFlashCardProgress;
import com.fabiocondo.dto.FlashCardDeckResponse;
import com.fabiocondo.dto.FlashCardProgressRequest;
import com.fabiocondo.dto.FlashCardResponse;
import com.fabiocondo.dto.SaveFlashCardRequest;
import com.fabiocondo.enumeration.FlashCardStatus;
import com.fabiocondo.repository.FlashCardRepository;
import com.fabiocondo.repository.UserFlashCardProgressRepository;
import com.fabiocondo.service.UserService;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class FlashCardServiceImpl {

    private final FlashCardRepository flashCardRepository;
    private final UserFlashCardProgressRepository progressRepository;
    private final UserService userService;

    public FlashCardServiceImpl(
            FlashCardRepository flashCardRepository,
            UserFlashCardProgressRepository progressRepository,
            UserService userService) {

        this.flashCardRepository = flashCardRepository;
        this.progressRepository = progressRepository;
        this.userService = userService;
    }

    public FlashCardDeckResponse getDeck(Long topicId, Long userId) {

        List<FlashCard> cards =
                flashCardRepository.findByTopic_Id(topicId);

        List<FlashCardResponse> responses =
                cards.stream()
                        .map(card -> mapToResponse(card, userId))
                        .collect(Collectors.toList());

        FlashCardDeckResponse response =
                new FlashCardDeckResponse();

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

    public void updateProgress(Long userId, FlashCardProgressRequest request) {

        UserFlashCardProgress progress = progressRepository
                .findByUser_IdAndFlashCard_Id(userId, request.getFlashCardId()).orElse(new UserFlashCardProgress());

        progress.setStatus(request.getStatus());
        progress.setReviewCount(
                Optional.ofNullable(progress.getReviewCount()).orElse(0) + 1);

        progress.setLastReviewedAt(LocalDateTime.now());

        progressRepository.save(progress);
    }

    public void saveFlashCard(Long userId, SaveFlashCardRequest request) {

        UserFlashCardProgress progress = progressRepository
                .findByUser_IdAndFlashCard_Id(userId, request.getFlashCardId()).orElse(new UserFlashCardProgress());

        progress.setSaved(request.isSaved());

        progressRepository.save(progress);
    }

    public List<FlashCardResponse> getSavedCards(Long userId) {

        List<UserFlashCardProgress> progresses = progressRepository.findByUserUserId(userId);

        return progresses.stream()
                .filter(UserFlashCardProgress::isSaved)
                .map(progress -> {

                    FlashCard card = progress.getFlashCard();

                    FlashCardResponse response = new FlashCardResponse();

                    response.setId(card.getId());
                    response.setQuestion(card.getQuestion());
                    response.setAnswer(card.getAnswer());
                    response.setCategory(card.getCategory());
                    response.setNote(card.getNote());

                    response.setSaved(true);

                    response.setStatus(
                            progress.getStatus() != null
                                    ? progress.getStatus()
                                    : FlashCardStatus.UNSEEN);

                    return response;
                })
                .collect(Collectors.toList());
    }

    private FlashCardResponse mapToResponse(FlashCard card, Long userId) {

        Optional<UserFlashCardProgress> progressOpt = progressRepository
                .findByUser_IdAndFlashCard_Id(userId, card.getId());

        FlashCardResponse response = new FlashCardResponse();

        response.setId(card.getId());
        response.setQuestion(card.getQuestion());
        response.setAnswer(card.getAnswer());
        response.setCategory(card.getCategory());
        response.setNote(card.getNote());

        if (progressOpt.isPresent()) {

            UserFlashCardProgress progress = progressOpt.get();

            response.setSaved(progress.isSaved());

            response.setStatus(
                    progress.getStatus() != null
                            ? progress.getStatus()
                            : FlashCardStatus.UNSEEN);

        } else {

            response.setSaved(false);
            response.setStatus(FlashCardStatus.UNSEEN);
        }

        return response;
    }

}
