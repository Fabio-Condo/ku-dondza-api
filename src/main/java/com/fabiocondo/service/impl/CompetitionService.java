package com.fabiocondo.service.impl;

import com.fabiocondo.domain.Competition;
import com.fabiocondo.domain.Question;
import com.fabiocondo.domain.User;
import com.fabiocondo.exception.domain.CompetitionNotFoundException;
import com.fabiocondo.exception.domain.QuestionNotFoundException;
import com.fabiocondo.exception.domain.UserNotFoundException;
import com.fabiocondo.repository.CompetitionRepository;
import com.fabiocondo.repository.QuestionRepository;
import com.fabiocondo.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class CompetitionService {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final CompetitionRepository competitionRepository;
    private final UserRepository userRepository;
    private final QuestionRepository questionRepository;

    public CompetitionService(CompetitionRepository competitionRepository, UserRepository userRepository, QuestionRepository questionRepository) {
        this.competitionRepository = competitionRepository;
        this.userRepository = userRepository;
        this.questionRepository = questionRepository;
    }

    public Competition createCompetition(Competition competition) {
        return competitionRepository.save(competition);
    }

    public Competition updateCompetition(Long id, Competition competition) throws CompetitionNotFoundException {
        Competition existingCompetition = getCompetitionById(id);
        BeanUtils.copyProperties(competition, existingCompetition, "id");
        return competitionRepository.save(existingCompetition);
    }

    public Page<Competition> findAll(String searchParam, Pageable pageable) {
        return competitionRepository.findAll(searchParam, pageable);
    }

    public Competition getCompetitionById(Long id) throws CompetitionNotFoundException {
        return competitionRepository.findById(id)
                .orElseThrow(() -> new CompetitionNotFoundException("Competition not found with ID: " + id));
    }

    public void delete(Long id) throws CompetitionNotFoundException {
        Competition existingCompetition = getCompetitionById(id);
        competitionRepository.deleteById(existingCompetition.getId());
    }

    public long getTotal(){
        return competitionRepository.count();
    }

    public Page<User> getParticipantsByCompetitionId(Long competitionId, Pageable pageable) throws CompetitionNotFoundException {
        Competition competition = getCompetitionById(competitionId);
        return competitionRepository.findParticipantsByCompetitionId(competition.getId(), pageable);
    }

    public Competition addParticipantToCompetition(Long competitionId, Long userId) throws UserNotFoundException, CompetitionNotFoundException {
        Competition competition = getCompetitionById(competitionId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("No user found by id: " + userId));
        competition.getParticipants().add(user);
        return competitionRepository.save(competition);
    }

    public Competition removeParticipantFromCompetition(Long competitionId, Long userId) throws UserNotFoundException, CompetitionNotFoundException {
        Competition competition = getCompetitionById(competitionId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("No user found by id: " + userId));
        competition.getParticipants().remove(user);
        return competitionRepository.save(competition);
    }

    public boolean checkIfIsParticipant(Long competitionId, Long userId) {
        Competition competition = competitionRepository.findById(competitionId).orElse(null);
        User user = userRepository.findById(userId).orElse(null);

        if (user == null || competition == null) {
            return false;
        }
        return competition.getParticipants().contains(user);
    }

    public long countParticipantsByCompetitionId(Long competitionId){
        return competitionRepository.countParticipantsByCompetitionId(competitionId);
    }

    public Page<Question> getQuestionsByCompetitionId(Long competitionId, Pageable pageable) throws CompetitionNotFoundException {
        Competition competition = competitionRepository.findById(competitionId)
                .orElseThrow(() -> new CompetitionNotFoundException("Competition not found with ID: " + competitionId));
        return competitionRepository.findQuestionsByCompetitionId(competition.getId(), pageable);
    }

    public Competition addQuestionToCompetition(Long competitionId, Long questionId) throws QuestionNotFoundException, CompetitionNotFoundException {
        Competition competition = getCompetitionById(competitionId);
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new QuestionNotFoundException("No question found by id: " + questionId));
        competition.getQuestions().add(question);
        return competitionRepository.save(competition);
    }

    public Competition removeQuestionFromCompetition(Long competitionId, Long questionId) throws QuestionNotFoundException, CompetitionNotFoundException {
        Competition competition = getCompetitionById(competitionId);
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new QuestionNotFoundException("No question found by id: " + questionId));
        competition.getQuestions().remove(question);
        return competitionRepository.save(competition);
    }

    public long countQuestionsByCompetitionId(Long competitionId){
        return competitionRepository.countQuestionsByCompetitionId(competitionId);
    }

    public void sendParticipationRequest(Long competitionId, Long userId) throws UserNotFoundException, CompetitionNotFoundException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("No user found by id: " + userId));
        Competition competition = getCompetitionById(competitionId);
        competition.getParticipationRequests().add(user);
        competitionRepository.save(competition);
    }

    public Competition acceptParticipationRequest(Long competitionId, Long userId) throws UserNotFoundException, CompetitionNotFoundException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("No user found by id: " + userId));
        Competition competition = getCompetitionById(competitionId);
        competition.getParticipationRequests().remove(user);
        competition.getParticipants().add(user);
        competitionRepository.save(competition);
        return competition;
    }

    public void rejectParticipationRequest(Long competitionId, Long userId) throws UserNotFoundException, CompetitionNotFoundException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("No user found by id: " + userId));
        Competition competition = getCompetitionById(competitionId);
        competition.getParticipationRequests().remove(user);
        competitionRepository.save(competition);
    }

    public Page<User> findParticipationRequestsByCompetitionId(Long competitionId, Pageable pageable) throws CompetitionNotFoundException {
        Competition competition = getCompetitionById(competitionId);
        return competitionRepository.findParticipationRequestsByCompetitionId(competition.getId(), pageable);
    }

    public Set<User> getParticipationRequest(Long competitionId) throws CompetitionNotFoundException {
        Competition competition = getCompetitionById(competitionId);
        return competition.getParticipationRequests();
    }

    public boolean checkIfRequestedParticipation(Long competitionId, Long userId) {
        Competition competition = competitionRepository.findById(competitionId).orElse(null);
        User user = userRepository.findById(userId).orElse(null);

        if (user == null || competition == null) {
            return false;
        }
        return competition.getParticipationRequests().contains(user);
    }

}

