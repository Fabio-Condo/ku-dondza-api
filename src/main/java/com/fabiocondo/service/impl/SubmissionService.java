package com.fabiocondo.service.impl;

import com.fabiocondo.domain.Answer;
import com.fabiocondo.domain.Competition;
import com.fabiocondo.domain.Submission;
import com.fabiocondo.domain.User;
import com.fabiocondo.exception.domain.CompetitionNotFoundException;
import com.fabiocondo.exception.domain.SubmissionNotFoundException;
import com.fabiocondo.exception.domain.UserNotFoundException;
import com.fabiocondo.repository.AnswerRepository;
import com.fabiocondo.repository.CompetitionRepository;
import com.fabiocondo.repository.SubmissionRepository;
import com.fabiocondo.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Service
public class SubmissionService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final SubmissionRepository submissionRepository;

    private final AnswerRepository answerRepository;

    private final CompetitionRepository competitionRepository;

    private final UserRepository userRepository;

    public SubmissionService(SubmissionRepository submissionRepository, AnswerRepository answerRepository, CompetitionRepository competitionRepository, UserRepository userRepository) {
        this.submissionRepository = submissionRepository;
        this.answerRepository = answerRepository;
        this.competitionRepository = competitionRepository;
        this.userRepository = userRepository;
    }

    public Submission findById(Long id) throws SubmissionNotFoundException {
        logger.info("Getting submission by id: " + id);
        return submissionRepository.findById(id)
                .orElseThrow(() -> new SubmissionNotFoundException("No submission found by id: " + id));
    }

    @Transactional
    public Submission create2(Submission submission, Set<Long> userAnswerIds) {

        Set<Answer> answers = new HashSet<>(answerRepository.findAllById(userAnswerIds));

        if (answers.size() != userAnswerIds.size()) {
            throw new IllegalArgumentException("Algumas respostas fornecidas são inválidas.");
        }

        if (!submission.getCompetition().getParticipants().contains(submission.getUser())) {
            throw new IllegalStateException("O usuário não está inscrito nesta competição.");
        }

        submission.setUserSubmittedAnswers(answers);
        submission.setSubmittedAt(new Date());

        return submissionRepository.save(submission);
    }

    @Transactional
    public Submission create(Submission submission, Set<Long> userAnswerIds) throws CompetitionNotFoundException, UserNotFoundException {

        logger.info("User: " + submission.getUser().getFirstName());

        Competition competition = competitionRepository.findById(submission.getCompetition().getId())
                .orElseThrow(() -> new CompetitionNotFoundException("Competição não encontrada."));

        User user = userRepository.findById(submission.getUser().getId())
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado."));

        if (!competition.getParticipants().contains(user)) {
            throw new IllegalStateException("O usuário não está inscrito nesta competição.");
        }

        if (submissionRepository.existsByCompetitionAndUser(competition, user)) {
            throw new IllegalStateException("O usuário já submeteu uma resposta para esta competição.");
        }

        Set<Answer> answers = new HashSet<>(answerRepository.findAllById(userAnswerIds));
        if (answers.size() != userAnswerIds.size()) {
            throw new IllegalArgumentException("Algumas respostas fornecidas são inválidas.");
        }

        submission.setUser(user);
        submission.setCompetition(competition);
        submission.setUserSubmittedAnswers(answers);
        submission.setSubmittedAt(new Date());

        return submissionRepository.save(submission);
    }



}
