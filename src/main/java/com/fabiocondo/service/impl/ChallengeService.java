package com.fabiocondo.service.impl;

import com.fabiocondo.domain.Challenge;
import com.fabiocondo.domain.Exam;
import com.fabiocondo.domain.Question;
import com.fabiocondo.exception.domain.ChallengeNotFoundException;
import com.fabiocondo.exception.domain.QuestionNotFoundException;
import com.fabiocondo.exception.domain.TopicNotFoundException;
import com.fabiocondo.repository.ChallengeRepository;
import com.fabiocondo.repository.QuestionRepository;
import com.fabiocondo.repository.filter.ChallengeFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;
import java.util.Set;

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

    public Challenge findByChallengeId(String challengeId) {
        return challengeRepository.findByChallengeId(challengeId)
                .orElseThrow(() -> new RuntimeException("Challenge não encontrado: " + challengeId));
    }

    public Page<Challenge> filter(ChallengeFilter challengeFilter, Pageable pageable) {
        //try {
        //    Thread.sleep(3000);
        //} catch (InterruptedException e) {
        //    Thread.currentThread().interrupt();
        //    throw new RuntimeException("A operação foi interrompida", e);
        //}
        return challengeRepository.filter(challengeFilter, pageable);
    }

    public Page<Challenge> findAll(Pageable pageable) {
        return challengeRepository.findAll(pageable);
    }

    public Challenge save(Challenge challenge) {
        return challengeRepository.save(challenge);
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
}