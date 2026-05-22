package com.fabiocondo.service.impl;

import com.fabiocondo.domain.Challenge;
import com.fabiocondo.domain.Question;
import com.fabiocondo.exception.domain.*;
import com.fabiocondo.repository.ChallengeRepository;
import com.fabiocondo.repository.QuestionRepository;
import com.fabiocondo.repository.filter.ChallengeFilter;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ChallengeService {

    private final ChallengeRepository challengeRepository;

    private final QuestionRepository questionRepository;


    public ChallengeService(ChallengeRepository challengeRepository, QuestionRepository questionRepository) {
        this.challengeRepository = challengeRepository;
        this.questionRepository = questionRepository;
    }

    public Challenge findById(Long id) throws ChallengeNotFoundException {
        return challengeRepository.findById(id)
                .orElseThrow(() -> new ChallengeNotFoundException("Challenge não encontrado: " + id));
    }

    public Challenge save(Challenge challenge) {
        challenge.setChallengeId(UUID.randomUUID().toString());
        return challengeRepository.save(challenge);
    }

    public Challenge update(Challenge challenge, Long id) throws ChallengeNotFoundException {
        Challenge existChallenge = findById(id);
        BeanUtils.copyProperties(challenge, existChallenge, "id", "challengeId", "challengeQuestions", "submittedChallengeQuizzes");
        return challengeRepository.save(existChallenge);
    }

    public Challenge findByChallengeId(String challengeId) throws ChallengeNotFoundException {
        return challengeRepository.findByChallengeId(challengeId)
                .orElseThrow(() -> new ChallengeNotFoundException("Challenge não encontrado: " + challengeId));
    }

    public Page<Challenge> filter(ChallengeFilter challengeFilter, Pageable pageable) {
        return challengeRepository.filter(challengeFilter, pageable);
    }

    public String getStatus(Challenge challenge) {
        Date now = new Date();

        if (challenge.getStartDate() != null && now.before(challenge.getStartDate())) {
            return "UPCOMING";
        }

        if (challenge.getEndDate() != null && now.after(challenge.getEndDate())) {
            return "DONE";
        }

        return "ONGOING";
    }

    public int getTotalQuestions(Challenge challenge) {
        return challenge.getChallengeQuestions() != null
                ? challenge.getChallengeQuestions().size()
                : 0;
    }

    public Integer getRemainingHours(Challenge challenge) {
        if (challenge.getEndDate() == null) return null;

        long diff = challenge.getEndDate().getTime() - new Date().getTime();

        if (diff <= 0) return 0;

        return (int) (diff / (1000 * 60 * 60));
    }

    public boolean isSubmitted(Challenge challenge) {
        return challenge.getSubmittedChallengeQuizzes() != null &&
                !challenge.getSubmittedChallengeQuizzes().isEmpty();
    }

    public int getTotalParticipants(Challenge challenge) {
        return challenge.getSubmittedChallengeQuizzes() != null
                ? challenge.getSubmittedChallengeQuizzes().size()
                : 0;
    }

    public Set<Question> getQuestionsByChallengeId(Long testId) throws TopicNotFoundException {

        Challenge challenge = challengeRepository.findById(testId)
                .orElseThrow(() -> new TopicNotFoundException("No test found by id: " + testId));

        return challenge.getChallengeQuestions();
    }

    public Challenge addQuestionToChallengeQuestions(Long challengeId, Long questionId) throws QuestionNotFoundException, ChallengeNotFoundException {
        Challenge challenge = findById(challengeId);
        Optional<Question> question = questionRepository.findById(questionId);
        if (!question.isPresent()){
            throw new QuestionNotFoundException("Question not found by id: " + questionId);
        }
        challenge.getChallengeQuestions().add(question.get());
        return challengeRepository.save(challenge);
    }

    public Challenge removeQuestionFromChallengeQuestions(Long challengeId, Long questionId) throws QuestionNotFoundException, ChallengeNotFoundException {
        Challenge challenge = findById(challengeId);
        Optional<Question> question = questionRepository.findById(questionId);
        if (!question.isPresent()) {
            throw new QuestionNotFoundException("Question not found by id: " + questionId);
        }
        challenge.getChallengeQuestions().remove(question.get());
        return challengeRepository.save(challenge);
    }

    public void validateUserHasNotSubmittedQuiz(Long challengeId, Long userId) throws UserAlreadySubmittedException {

        boolean alreadySubmitted = challengeRepository
                .existsQuizInChallenge(challengeId, userId);

        if (alreadySubmitted) {
            throw new UserAlreadySubmittedException(
                    "Voçê já submeteu este desafio."
            );
        }
    }

    public boolean hasUserSubmitted(Challenge challenge, Long currentUserId) {

        if (challenge == null
                || challenge.getId() == null
                || currentUserId == null) {
            return false;
        }

        return challengeRepository.existsQuizInChallenge(
                challenge.getId(),
                currentUserId
        );
    }

    public void validateChallengeAvailability(Long challengeId)
            throws ChallengeNotFoundException, ChallengeUnavailableException {

        Challenge challenge = findById(challengeId);

        Date now = new Date();

        if (challenge.getStartDate() != null && now.before(challenge.getStartDate())) {
            throw new ChallengeUnavailableException(
                    "Este desafio ainda não começou."
            );
        }

        if (challenge.getEndDate() != null && now.after(challenge.getEndDate())) {
            throw new ChallengeUnavailableException(
                    "Este desafio já foi encerrado."
            );
        }
    }
}