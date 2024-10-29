package com.fabiocondo.service.impl;

import com.fabiocondo.domain.Competition;
import com.fabiocondo.domain.Question;
import com.fabiocondo.domain.User;
import com.fabiocondo.exception.domain.CompetitionNotFoundException;
import com.fabiocondo.exception.domain.QuestionNotFoundException;
import com.fabiocondo.exception.domain.UserNotFoundException;
import com.fabiocondo.repository.CompetitionRepository;
import com.fabiocondo.repository.QuestionRepository;
import com.fabiocondo.repository.QuizRepository;
import com.fabiocondo.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class CompetitionService {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final CompetitionRepository competitionRepository;
    private final QuizRepository quizRepository;
    private final UserRepository userRepository;

    private final QuestionRepository questionRepository;

    public CompetitionService(CompetitionRepository competitionRepository, QuizRepository quizRepository, UserRepository userRepository, QuestionRepository questionRepository) {
        this.competitionRepository = competitionRepository;
        this.quizRepository = quizRepository;
        this.userRepository = userRepository;
        this.questionRepository = questionRepository;
    }

    public Competition createCompetition(Competition competition) {
        return competitionRepository.save(competition);
    }

    public Competition updateCompetition(Long id, Competition competition) {
        Competition existingCompetition = getCompetitionById(id);
        BeanUtils.copyProperties(competition, existingCompetition, "id");
        return competitionRepository.save(existingCompetition);
    }

    public Page<Competition> findAll(Pageable pageable) {
        return competitionRepository.findAll(pageable);
    }

    public Competition getCompetitionById(Long id) {
        return competitionRepository.findById(id).orElse(null);
    }

    public void delete(Long id) {
        Competition existingCompetition = getCompetitionById(id);
        competitionRepository.deleteById(existingCompetition.getId());
    }

    public long getTotal(){
        return competitionRepository.count();
    }

    public Page<User> getParticipantsByCompetitionId(Long competitionId, Pageable pageable) throws CompetitionNotFoundException {
        Competition competition = competitionRepository.findById(competitionId)
                .orElseThrow(() -> new CompetitionNotFoundException("Competition not found with ID: " + competitionId));
        return competitionRepository.findParticipantsByCompetitionId(competition.getId(), pageable);
    }

    public Competition addParticipantToCompetition(Long competitionId, Long userId) throws UserNotFoundException {
        Competition competition = getCompetitionById(competitionId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("No user found by id: " + userId));
        competition.getParticipants().add(user);
        return competitionRepository.save(competition);
    }

    public Competition removeParticipantFromCompetition(Long competitionId, Long userId) throws UserNotFoundException {
        Competition competition = getCompetitionById(competitionId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("No user found by id: " + userId));
        competition.getParticipants().remove(user);
        return competitionRepository.save(competition);
    }

    public Page<Question> getQuestionsByCompetitionId(Long competitionId, Pageable pageable) throws CompetitionNotFoundException {
        Competition competition = competitionRepository.findById(competitionId)
                .orElseThrow(() -> new CompetitionNotFoundException("Competition not found with ID: " + competitionId));
        return competitionRepository.findQuestionsByCompetitionId(competition.getId(), pageable);
    }

    public Competition addQuestionToCompetition(Long competitionId, Long userId) throws QuestionNotFoundException {
        Competition competition = getCompetitionById(competitionId);
        Question question = questionRepository.findById(userId)
                .orElseThrow(() -> new QuestionNotFoundException("No question found by id: " + userId));
        competition.getQuestions().add(question);
        return competitionRepository.save(competition);
    }

    public Competition removeQuestionFromCompetition(Long competitionId, Long userId) throws QuestionNotFoundException {
        Competition competition = getCompetitionById(competitionId);
        Question question = questionRepository.findById(userId)
                .orElseThrow(() -> new QuestionNotFoundException("No question found by id: " + userId));
        competition.getQuestions().remove(question);
        return competitionRepository.save(competition);
    }
}

