package com.fabiocondo.dtoMapper;

import com.fabiocondo.domain.Challenge;
import com.fabiocondo.dto.ChallengeDTO;
import com.fabiocondo.service.impl.ChallengeService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class ChallengeMapper {

    private final ChallengeService challengeService;

    public ChallengeMapper(ChallengeService challengeService) {
        this.challengeService = challengeService;
    }

    public ChallengeDTO toResponse(Challenge challenge) {

        ChallengeDTO response = new ChallengeDTO();

        response.setId(challenge.getId());
        response.setChallengeId(challenge.getChallengeId());
        response.setTitle(challenge.getTitle());
        response.setDescription(challenge.getDescription());

        response.setDifficultyLevel(challenge.getDifficultyLevel());
        response.setXpReward(challenge.getXpReward());

        response.setStartDate(challenge.getStartDate());
        response.setEndDate(challenge.getEndDate());

        response.setStatus(challengeService.getStatus(challenge));
        response.setTotalQuestions(challengeService.getTotalQuestions(challenge));
        response.setRemainingHours(challengeService.getRemainingHours(challenge));
        response.setSubmitted(challengeService.isSubmitted(challenge));
        response.setTotalParticipants(challengeService.getTotalParticipants(challenge));

        if (challenge.getSubject() != null) {
            response.setSubjectName(challenge.getSubject().getName());
        }

        return response;
    }

    public Page<ChallengeDTO> toResponsePage(Page<Challenge> page) {
        return page.map(this::toResponse);
    }
}