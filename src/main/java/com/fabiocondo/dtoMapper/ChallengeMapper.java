package com.fabiocondo.dtoMapper;

import com.fabiocondo.domain.Challenge;
import com.fabiocondo.domain.Quiz;
import com.fabiocondo.domain.User;
import com.fabiocondo.dto.ChallengeSummaryDTO;
import com.fabiocondo.dto.ChallengeRankingResultDTO;
import com.fabiocondo.repository.UserRepository;
import com.fabiocondo.service.impl.ChallengeService;
import com.fabiocondo.service.impl.QuizService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.Optional;


@Component
public class ChallengeMapper {

    private final ChallengeService challengeService;

    private final QuizService quizService;

    private final UserRepository userRepository;

    public ChallengeMapper(ChallengeService challengeService, QuizService quizService, UserRepository userRepository) {
        this.challengeService = challengeService;
        this.quizService = quizService;
        this.userRepository = userRepository;
    }

    public ChallengeSummaryDTO toResponse(Challenge challenge, Long currentUserId) {

        ChallengeSummaryDTO response = new ChallengeSummaryDTO();

        int totalQuestions = challengeService.getTotalQuestions(challenge);
        Optional<User> currentUser = userRepository.findById(currentUserId);

        if(currentUser.isPresent()){
            response.setHasCurrentUserSubmitted(challengeService.hasUserSubmitted(challenge, currentUserId));
        }

        response.setId(challenge.getId());
        response.setChallengeId(challenge.getChallengeId());
        response.setTitle(challenge.getTitle());
        response.setDescription(challenge.getDescription());
        response.setSubject(challenge.getSubject());

        response.setDifficultyLevel(challenge.getDifficultyLevel());
        response.setXpReward(totalQuestions * 10);

        response.setStartDate(challenge.getStartDate());
        response.setEndDate(challenge.getEndDate());

        response.setStatus(challengeService.getStatus(challenge));
        response.setTotalQuestions(totalQuestions);
        response.setRemainingHours(challengeService.getRemainingHours(challenge));
        response.setTotalParticipants(challengeService.getTotalParticipants(challenge));

        if (challenge.getSubject() != null) {
            response.setSubjectName(challenge.getSubject().getName());
        }

        //response.setCurrentUserRank();
        //response.setCurrentUserScore();

        return response;
    }

    public Page<ChallengeSummaryDTO> toResponsePage(Page<Challenge> page, Long currentUserId) {
        return page.map(challenge -> toResponse(challenge, currentUserId));
    }

    public ChallengeRankingResultDTO toRankingResponse(Quiz quiz, Integer rankingPosition) {
        ChallengeRankingResultDTO dto = new ChallengeRankingResultDTO();

        User user = quiz.getUser();

        int totalQuestions = quiz.getQuestions().size();
        int correctAnswers = quizService.countCorrectAnswers(quiz);

        dto.setUserId(user.getId());
        dto.setUsername(user.getFullName());

        dto.setRankingPosition(rankingPosition);

        dto.setTotalQuestions(totalQuestions);

        dto.setCorrectAnswers(correctAnswers);

        dto.setPercentage(
                totalQuestions == 0
                        ? 0.0
                        : ((double) correctAnswers / totalQuestions) * 100
        );

        dto.setTimeSpent(quiz.getTimeSpent());

        // cada acerto vale 10 XP
        dto.setXpEarned(correctAnswers * 10L);

        dto.setQuizId(quiz.getQuizId());

        return dto;
    }
}