package com.fabiocondo.service.impl;

import com.fabiocondo.domain.*;
import com.fabiocondo.dto.RankingDTO;
import com.fabiocondo.enumeration.CompetitionType;
import com.fabiocondo.exception.domain.*;
import com.fabiocondo.repository.AnswerRepository;
import com.fabiocondo.repository.CompetitionRepository;
import com.fabiocondo.repository.SubmissionRepository;
import com.fabiocondo.repository.UserRepository;
import com.fabiocondo.repository.filter.SubmissionFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

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

    public Optional<Submission> findSubmissionByUserAndCompetition(Long userId, Long competitionId) {
        return submissionRepository.findByUserIdAndCompetitionId(userId, competitionId);
    }

    public Page<Submission> filter(SubmissionFilter submissionFilter, Pageable pageable) {
        return submissionRepository.filter(submissionFilter, pageable);
    }

    public Page<RankingDTO> getRanking(Long competitionId, Pageable pageable) {
        return submissionRepository.getRanking(competitionId, pageable);
    }

    public Page<Submission> findAll(Pageable pageable) {
        return submissionRepository.findAll(pageable);
    }

    public Page<Submission> findAllByCompetitionId(Long competitionId, Pageable pageable) {
        return submissionRepository.findAllByCompetitionId(competitionId, pageable);
    }

    public Long countByCompetitionId(Long competitionId) {
        return submissionRepository.countByCompetitionId(competitionId);
    }

    @Transactional
    public Submission create(Submission submission, Set<Long> userAnswerIds)
            throws CompetitionNotFoundException, UserNotFoundException, UserAlreadySubmittedException, ClosedSubmissionException, CompetitionUserUnauthorizedExceptionException {

        Long competitionId = submission.getCompetition().getId();
        Long userId = submission.getUser().getId();

        if (hasUserAlreadySubmitted(competitionId, userId)) {
            throw new UserAlreadySubmittedException("O usuário já submeteu uma resposta para esta competição.");
        }

        Competition competition = competitionRepository.findById(competitionId)
                .orElseThrow(() -> new CompetitionNotFoundException("Competição não encontrada."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado."));

        if(!competition.isOpen()){
            throw new ClosedSubmissionException("Competição encerrada.");
        }

        if (competition.getCompetitionType().requiresAuthorization()) {
            if (!competition.getAllowedUsers().contains(user)) {
                throw new CompetitionUserUnauthorizedExceptionException("Você não está autorizado a participar desta competição.");
            }
        }

        Set<Answer> answers = new HashSet<>(answerRepository.findAllById(userAnswerIds));
        if (answers.size() != userAnswerIds.size()) {
        //    throw new ClosedSubmissionException("Algumas respostas fornecidas são inválidas.");
        }

        submission.setUser(user);
        submission.setCompetition(competition);
        submission.setAnswers(answers);
        submission.setSubmittedAt(new Date()); // Last update

        return submissionRepository.save(submission);
    }

    @Transactional
    public Submission update(Long submissionId, Set<Long> userAnswerIds)
            throws CompetitionNotFoundException, UserNotFoundException, ClosedSubmissionException, CompetitionUserUnauthorizedExceptionException, SubmissionNotFoundException {

        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new SubmissionNotFoundException("Submissão não encontrada."));

        Long competitionId = submission.getCompetition().getId();
        Long userId = submission.getUser().getId();

        Competition competition = competitionRepository.findById(competitionId)
                .orElseThrow(() -> new CompetitionNotFoundException("Competição não encontrada."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado."));

        if(!competition.isOpen()){
            throw new ClosedSubmissionException("Competição encerrada.");
        }

        if (competition.getCompetitionType().requiresAuthorization()) {
            if (!competition.getAllowedUsers().contains(user)) {
                throw new CompetitionUserUnauthorizedExceptionException("Você não está autorizado a participar desta competição.");
            }
        }

        Set<Answer> answers = new HashSet<>(answerRepository.findAllById(userAnswerIds));
        submission.setAnswers(answers);

        submission.setSubmittedAt(new Date()); // Last update

        return submissionRepository.save(submission);
    }

    public boolean isUserAllowedToParticipate(Long competitionId, Long userId){
        Optional<Competition> competitionOpt = competitionRepository.findById(competitionId);
        Optional<User> userOpt = userRepository.findById(userId);

        if (!competitionOpt.isPresent() || !userOpt.isPresent()) {
            return false;
        }

        // Se o tipo da competição exige autorização, verifica a lista de permitidos
        if (competitionOpt.get().getCompetitionType().requiresAuthorization()) {
            return competitionOpt.get().getAllowedUsers() != null && competitionOpt.get().getAllowedUsers().contains(userOpt.get());
        }

        // Caso contrário, a participação é liberada
        return true;
    }

    public boolean hasUserAlreadySubmitted(Long competitionId, Long userId) {
        Optional<Competition> competitionOpt = competitionRepository.findById(competitionId);
        Optional<User> userOpt = userRepository.findById(userId);

        if (!competitionOpt.isPresent() || !userOpt.isPresent()) {
            return false;
        }

        return submissionRepository.existsByCompetitionAndUser(competitionOpt.get(), userOpt.get());
    }

    // Método para calcular o total de respostas corretas para uma submissão
    public long getTotalCorrectAnswersForSubmission(Long submissionId) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new IllegalArgumentException("Submissão não encontrada"));

        return submission.getAnswers().stream()
                .filter(Answer::isCorrect) // Filtra as respostas corretas
                .count(); // Conta as respostas corretas
    }

    //@Transactional
    //public Submission create(Submission submission, Set<Long> userAnswerIds) throws CompetitionNotFoundException, UserNotFoundException {

    //    Competition competition = competitionRepository.findById(submission.getCompetition().getId())
    //            .orElseThrow(() -> new CompetitionNotFoundException("Competição não encontrada."));

    //    User user = userRepository.findById(submission.getUser().getId())
    //            .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado."));

    //    if (submissionRepository.existsByCompetitionAndUser(competition, user)) {
    //        throw new IllegalStateException("O usuário já submeteu uma resposta para esta competição.");
    //    }

    //    Set<Answer> answers = new HashSet<>(answerRepository.findAllById(userAnswerIds));
    //    if (answers.size() != userAnswerIds.size()) {
    //        throw new IllegalArgumentException("Algumas respostas fornecidas são inválidas.");
    //    }

    //    submission.setUser(user);
    //    submission.setCompetition(competition);
    //    submission.setAnswers(answers);
    //    submission.setSubmittedAt(new Date());

    //    return submissionRepository.save(submission);
    //}

}
