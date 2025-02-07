package com.fabiocondo.service.impl;

import com.fabiocondo.domain.*;
import com.fabiocondo.enumeration.CompetitionStatus;
import com.fabiocondo.enumeration.RankingPosition;
import com.fabiocondo.exception.domain.CompetitionCannotBeFinishedException;
import com.fabiocondo.exception.domain.CompetitionNotFoundException;
import com.fabiocondo.exception.domain.UserNotFoundException;
import com.fabiocondo.repository.*;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

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
    public Competition createCompetition(Competition competition, Set<Long> topicIds) {

        if (topicIds == null || topicIds.isEmpty()) {
            throw new IllegalArgumentException("A Competition deve ter pelo menos um tópico associado.");
        }

        Set<Topic> topics = new HashSet<>(topicRepository.findAllById(topicIds));

        if (topics.size() != topicIds.size()) {
            throw new IllegalArgumentException("Um ou mais tópicos não foram encontrados no banco de dados.");
        }

        Set<Question> questions = questionRepository.findByTopicIdIn(topicIds);
        competition.setQuestions(questions);

        competition.setCompetitionId(generateCompetitionId());
        competition.setStatus(CompetitionStatus.PLANNING);
        competition.getPrizes().forEach(prize -> prize.setCompetition(competition));

        return competitionRepository.save(competition);
    }

    @Transactional
    public Competition updateCompetition(Long id, Competition competition, Set<Long> topicIds, Boolean generateQuestions) throws CompetitionNotFoundException {

        Competition existingCompetition = getCompetitionById(id);

        //if(!existingCompetition.getSubmissions().isEmpty()) {
        //    throw new IllegalArgumentException("A Competition não pode ser actualizada. Contém submissões.");
        //}
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

            Set<Question> questions = questionRepository.findByTopicIdIn(topicIds);
            existingCompetition.setQuestions(questions);
        }

        existingCompetition.getPrizes().clear();
        existingCompetition.getPrizes().addAll(competition.getPrizes());
        existingCompetition.getPrizes().forEach(prize -> prize.setCompetition(existingCompetition));

        BeanUtils.copyProperties(competition, existingCompetition, "id", "competitionId", "questions", "participants", "prizes", "submissions", "winners");

        return competitionRepository.save(existingCompetition);
    }

    public Page<Competition> findAll(String searchParam, Pageable pageable) {
        return competitionRepository.findAll(searchParam, pageable);
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

    public boolean checkIfRequestedParticipation(Long competitionId, Long userId) {
        Competition competition = competitionRepository.findById(competitionId).orElse(null);
        User user = userRepository.findById(userId).orElse(null);

        if (user == null || competition == null) {
            return false;
        }
        return competition.getParticipationRequests().contains(user);
    }

    public void initCompetition(Long competitionId) throws CompetitionNotFoundException, CompetitionCannotBeFinishedException {
        Competition competition = getCompetitionById(competitionId);

        // Só permite mudar para ONGOING se estiver PLANNING
        if (competition.getStatus() != CompetitionStatus.PLANNING) {
            throw new CompetitionCannotBeFinishedException("A competição só pode ser inicializada se estiver em planeamento (PLANNING).");
        }

        // Atualiza o status
        competition.setStatus(CompetitionStatus.ONGOING);
        competition.setStartedAt(new Date());
        competitionRepository.save(competition);
    }

    @Transactional
    public void finishCompetition(Long competitionId) throws CompetitionNotFoundException, CompetitionCannotBeFinishedException {
        Competition competition = getCompetitionById(competitionId);

        // Só permite mudar para COMPLETED se estiver ONGOING
        if (competition.getStatus() != CompetitionStatus.ONGOING) {
            throw new CompetitionCannotBeFinishedException("A competição só pode ser finalizada se estiver em andamento (ONGOING).");
        }

        // Atualiza o status
        competition.setStatus(CompetitionStatus.FINISHED);
        competition.setEndedAt(new Date());
        competitionRepository.save(competition);

        // Definir os vencedores
        defineWinners(competitionId);
    }

    @Transactional
    public void defineWinners(Long competitionId) throws CompetitionNotFoundException {
        Competition competition = getCompetitionById(competitionId);

        // 1. Remover todos os vencedores anteriores
        competition.getWinners().clear();
        competitionRepository.save(competition); // Garantir que a remoção seja persistida

        // 2. Obter todas as submissões da competição
        List<Submission> submissions = submissionRepository.findByCompetitionId(competitionId);

        // 3. Ordenar as submissões pelo total de acertos (decrescente) e pela data de submissão (mais antiga primeiro)
        List<Submission> sortedSubmissions = submissions.stream()
                .sorted(Comparator
                        .comparingLong(Submission::getTotalCorrectAnswers).reversed()
                        .thenComparing(Submission::getSubmittedAt)
                )
                .collect(Collectors.toList());

        // 4. Obter os prêmios da competição ordenados por posição (FIRST_PLACE, SECOND_PLACE, etc.)
        List<Prize> prizes = competition.getPrizes().stream()
                .sorted(Comparator.comparing(Prize::getPosition))
                .collect(Collectors.toList());

        // 5. Definir os vencedores com base nas posições dos prêmios
        for (int i = 0; i < prizes.size() && i < sortedSubmissions.size(); i++) {
            Prize prize = prizes.get(i);
            Submission submission = sortedSubmissions.get(i);

            // Criar um novo CompetitionWinner
            CompetitionWinner winner = new CompetitionWinner();
            winner.setCompetition(competition);
            winner.setUser(submission.getUser());
            winner.setPrize(prize);

            // Adicionar o vencedor à lista de vencedores da competição
            competition.getWinners().add(winner);
        }

        // 6. Salvar a competição com os vencedores atualizados
        competitionRepository.save(competition);
    }

    // Verifica as competições em andamento que estão prestes a terminar
    //@Scheduled(fixedRate = 600000) // 10 minutos
    //@Scheduled(fixedRate = 60000) // 1 minutos
    @Transactional  // Garantir que a transação seja aberta
    public void checkAndFinishCompetitions() throws CompetitionCannotBeFinishedException, CompetitionNotFoundException {
        // Buscar competições em andamento
        List<Competition> ongoingCompetitions = competitionRepository.findByStatus(CompetitionStatus.ONGOING);

        for (Competition competition : ongoingCompetitions) {
            // Verificar se já passou o tempo de término
            if (competition.getEndedAt().before(new Date())) {
                // Finalizar a competição
                finishCompetition(competition.getId());
            }
        }
    }

    private String generateCompetitionId() {
        return RandomStringUtils.randomAlphanumeric(10);
    }
}

