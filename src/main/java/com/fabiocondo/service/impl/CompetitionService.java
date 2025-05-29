package com.fabiocondo.service.impl;

import com.fabiocondo.domain.*;
import com.fabiocondo.exception.domain.*;
import com.fabiocondo.repository.*;
import com.fabiocondo.repository.filter.CompetitionFilter;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;


@Service
public class CompetitionService {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final CompetitionRepository competitionRepository;
    private final UserRepository userRepository;
    private final QuestionRepository questionRepository;
    private final TopicRepository topicRepository;
    private final SubmissionRepository submissionRepository;

    public CompetitionService(CompetitionRepository competitionRepository, UserRepository userRepository, QuestionRepository questionRepository, TopicRepository topicRepository, SubmissionRepository submissionRepository) {
        this.competitionRepository = competitionRepository;
        this.userRepository = userRepository;
        this.questionRepository = questionRepository;
        this.topicRepository = topicRepository;
        this.submissionRepository = submissionRepository;
    }

    @Transactional
    public Competition createCompetition(Competition competition, Set<Long> topicIds) throws UserNotFoundException {

        if (topicIds == null || topicIds.isEmpty()) {
            throw new IllegalArgumentException("A Competition deve ter pelo menos um tópico associado.");
        }

        Set<Topic> topics = new HashSet<>(topicRepository.findAllById(topicIds));

        if (topics.size() != topicIds.size()) {
            throw new IllegalArgumentException("Um ou mais tópicos não foram encontrados no banco de dados.");
        }

        Set<Question> questions = questionRepository.findRandomQuestionsByTopicsAndDifficulty(topicIds, competition.getDifficultyLevel(), 2);
        competition.setQuestions(questions);
        competition.setCompetitionId(UUID.randomUUID().toString());

        return competitionRepository.save(competition);
    }

    @Transactional
    public Competition updateCompetition(Long id, Competition competition, Set<Long> topicIds, Boolean generateQuestions) throws CompetitionNotFoundException {

        Competition existingCompetition = getCompetitionById(id);

        if(!existingCompetition.getSubmissions().isEmpty()) {
            throw new IllegalArgumentException("A Competition não pode ser actualizada. Contém submissões.");
        }
        //if (existingCompetition.getStatus().equals(CompetitionStatus.ONGOING) || existingCompetition.getStatus().equals(CompetitionStatus.FINISHED)) {
        //    throw new IllegalArgumentException("A Competition não pode ser actualizada. Está em andamento ou finalizada.");
        //}

        if (generateQuestions) {
            if (topicIds == null || topicIds.isEmpty()) {
                throw new IllegalArgumentException("A Competition deve ter pelo menos um tópico associado.");
            }

            Set<Topic> topics = new HashSet<>(topicRepository.findAllById(topicIds));

            if (topics.size() != topicIds.size()) {
                throw new IllegalArgumentException("Um ou mais tópicos não foram encontrados no banco de dados.");
            }

            existingCompetition.getQuestions().clear();
            competitionRepository.save(existingCompetition); // Garantir que a remoção seja persistida

            Set<Question> questions = questionRepository.findRandomQuestionsByTopicsAndDifficulty(topicIds, competition.getDifficultyLevel(), 2);
            existingCompetition.setQuestions(questions);
            existingCompetition.setDifficultyLevel(competition.getDifficultyLevel());
        }

        BeanUtils.copyProperties(competition, existingCompetition, "id", "competitionId", "questions", "participants", "prizes", "submissions", "winners");

        return competitionRepository.save(existingCompetition);
    }

    public Page<Competition> findAll(String searchParam, Pageable pageable) {
        return competitionRepository.findAll(searchParam, pageable);
    }

    public Page<Competition> filter(CompetitionFilter quizFilter, Pageable pageable) {
        return competitionRepository.filter(quizFilter, pageable);
    }

    public Page<Competition> getCompetitionsByQuestionId(Long questionId, Pageable pageable) throws QuizNotFoundException {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new QuizNotFoundException("No question found by id: " + questionId));
        return competitionRepository.findAllByQuestions(question, pageable);
    }

    public Competition getCompetitionById(Long id) throws CompetitionNotFoundException {
        return competitionRepository.findById(id)
                .orElseThrow(() -> new CompetitionNotFoundException("No competition found by id: " + id));
    }

    public Competition findCompetitionByCompetitionId(String competitionId) throws CompetitionNotFoundException {
        return competitionRepository.findCompetitionByCompetitionId(competitionId)
                .orElseThrow(() -> new CompetitionNotFoundException("No competition found by id: " + competitionId));
    }

    public void delete(Long id) throws CompetitionNotFoundException {
        Competition existingCompetition = getCompetitionById(id);
        competitionRepository.deleteById(existingCompetition.getId());
    }

    public long getTotal(){
        return competitionRepository.count();
    }

    public Set<Topic> getTopicsByCompetitionId(Long competitionId) {
        // Buscar competição pelo ID
        Competition competition = competitionRepository.findById(competitionId)
                .orElseThrow(() -> new RuntimeException("Competição não encontrada"));

        // Usar um Set para garantir que não haja tópicos duplicados
        Set<Topic> topics = new HashSet<>();

        // Iterar sobre as questões e adicionar os tópicos associados
        competition.getQuestions().forEach(question -> {
            if (question.getTopic() != null) {
                topics.add(question.getTopic());
            }
        });

        return topics;
    }

    public Page<Question> getQuestionsByCompetitionId(Long competitionId, Pageable pageable) throws CompetitionNotFoundException {
        Competition competition = competitionRepository.findById(competitionId)
                .orElseThrow(() -> new CompetitionNotFoundException("Competition not found with ID: " + competitionId));
        return competitionRepository.findQuestionsByCompetitionId(competition.getId(), pageable);
    }

    public long countQuestionsByCompetitionId(Long competitionId){
        return competitionRepository.countQuestionsByCompetitionId(competitionId);
    }
}

