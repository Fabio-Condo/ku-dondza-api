package com.fabiocondo.controller;

import com.fabiocondo.domain.*;
import com.fabiocondo.exception.domain.*;
import com.fabiocondo.repository.filter.CompetitionFilter;
import com.fabiocondo.service.impl.CompetitionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/competitions")
public class CompetitionController {

    private final CompetitionService competitionService;

    public CompetitionController(CompetitionService competitionService) {
        this.competitionService = competitionService;
    }

    @PostMapping
    public ResponseEntity<Competition> createCompetition(@RequestBody Competition competition,
                                                         @RequestParam Set<Long> topicIds) throws UserNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(competitionService.createCompetition(competition, topicIds));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Competition> updateCompetition(@PathVariable("id") Long id,
                                                         @RequestBody Competition competition,
                                                         @RequestParam Set<Long> topicIds,
                                                         @RequestParam Boolean generateQuestions) throws CompetitionNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(competitionService.updateCompetition(id, competition, topicIds, generateQuestions));
    }

    @GetMapping("/filter")
    public Page<Competition> filter(CompetitionFilter competitionFilter, Pageable pageable) {
        return competitionService.filter(competitionFilter, pageable);
    }

    @GetMapping("/findAll")
    public ResponseEntity<Page<Competition>> findAll(@RequestParam(required = false, defaultValue = "") String searchParam, Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(competitionService.findAll(searchParam, pageable));
    }

    @GetMapping("/by-question/{questionId}")
    public Page<Competition> getQuizzesByQuestionId(@PathVariable("questionId") Long questionId, Pageable pageable) throws QuizNotFoundException {
        return competitionService.getQuizzesByQuestionId(questionId, pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Competition> getCompetitionById(@PathVariable Long id) throws CompetitionNotFoundException {
        return ResponseEntity.ok().body(competitionService.getCompetitionById(id));
    }

    @GetMapping("/find-by-competitionId/{competitionId}")
    public ResponseEntity<Competition> findCompetitionByCompetitionId(@PathVariable("competitionId") String competitionId) throws CompetitionNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(competitionService.findCompetitionByCompetitionId(competitionId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") Long id) throws CompetitionNotFoundException {
        competitionService.delete(id);
        return response(HttpStatus.OK, "Competition deleted successfully");
    }

    @GetMapping("/total")
    public ResponseEntity<Long> getTotal(){
        return ResponseEntity.status(HttpStatus.OK).body(competitionService.getTotal());
    }

    @GetMapping("/{competitionId}/participants")
    public Page<User> getParticipantsByCompetitionId(@PathVariable Long competitionId, Pageable pageable) throws CompetitionNotFoundException {
        return competitionService.getParticipantsByCompetitionId(competitionId, pageable);
    }

    @PostMapping("/{competitionId}/participants/{userId}")
    public ResponseEntity<Competition> addParticipantToCompetition(@PathVariable Long competitionId, @PathVariable Long userId) throws UserNotFoundException, CompetitionNotFoundException {
        Competition updatedCompetition = competitionService.addParticipantToCompetition(competitionId, userId);
        return updatedCompetition != null ? ResponseEntity.ok(updatedCompetition) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{competitionId}/participants/{userId}")
    public ResponseEntity<Competition> removeParticipantFromCompetition(@PathVariable Long competitionId, @PathVariable Long userId) throws UserNotFoundException, CompetitionNotFoundException {
        Competition updatedCompetition = competitionService.removeParticipantFromCompetition(competitionId, userId);
        return updatedCompetition != null ? ResponseEntity.ok(updatedCompetition) : ResponseEntity.notFound().build();
    }

    @GetMapping("{competitionId}/participants/contains/{userId}")
    public ResponseEntity<Boolean> checkIfIsParticipant(@PathVariable Long competitionId, @PathVariable Long userId) {
        boolean isParticipant = competitionService.checkIfIsParticipant(competitionId, userId);
        return ResponseEntity.ok(isParticipant);
    }

    @GetMapping("/{competitionId}/participants/total")
    public ResponseEntity<Long> countParticipantsByCompetitionId(@PathVariable Long competitionId){
        return ResponseEntity.status(HttpStatus.OK).body(competitionService.countParticipantsByCompetitionId(competitionId));
    }

    @GetMapping("/{competitionId}/topics")
    public ResponseEntity<Set<Topic>> getTopicsByCompetitionId(@PathVariable Long competitionId) {
        Set<Topic> topics = competitionService.getTopicsByCompetitionId(competitionId);
        return ResponseEntity.ok(topics);
    }

    @GetMapping("/{competitionId}/questions")
    public Page<Question> getQuestionsByCompetitionId(@PathVariable Long competitionId, Pageable pageable) throws CompetitionNotFoundException {
        return competitionService.getQuestionsByCompetitionId(competitionId, pageable);
    }

    @GetMapping("/{competitionId}/questions/total")
    public ResponseEntity<Long> countQuestionsByCompetitionId(@PathVariable Long competitionId){
        return ResponseEntity.status(HttpStatus.OK).body(competitionService.countQuestionsByCompetitionId(competitionId));
    }

    @PostMapping("/{competitionId}/send-participation-invite/{userId}")
    public ResponseEntity<?> sendParticipationInvite(@PathVariable Long competitionId, @PathVariable Long userId) throws UserNotFoundException, CompetitionNotFoundException, UserInvitedException {
        competitionService.sendParticipationInvite(competitionId, userId);
        return response(HttpStatus.OK, "Participation invite sent successfully");
    }

    @PostMapping("/{competitionId}/accept-participation-invite/{userId}")
    public Competition acceptParticipationInvite(@PathVariable Long competitionId, @PathVariable Long userId) throws UserNotFoundException, CompetitionNotFoundException, ParticipationInviteNotFoundException {
        return competitionService.acceptParticipationInvite(competitionId, userId);
    }

    @PostMapping("/{competitionId}/send-participation-request/{userId}")
    public void sendParticipationRequest(@PathVariable Long competitionId, @PathVariable Long userId) throws UserNotFoundException, CompetitionNotFoundException {
        competitionService.sendParticipationRequest(competitionId, userId);
    }

    @PostMapping("/{competitionId}/accept-participation-requests/{userId}")
    public Competition acceptParticipationRequest(@PathVariable Long competitionId, @PathVariable Long userId) throws UserNotFoundException, CompetitionNotFoundException {
        return competitionService.acceptParticipationRequest(competitionId, userId);
    }

    @DeleteMapping("/{competitionId}/reject-participation-requests/{userId}")
    public void rejectParticipationRequest(@PathVariable Long competitionId, @PathVariable Long userId) throws UserNotFoundException, CompetitionNotFoundException {
        competitionService.rejectParticipationRequest(competitionId, userId);
    }

    @GetMapping("/{competitionId}/participation-requests")
    public Page<User> findParticipationRequestsByCompetitionId(@PathVariable Long competitionId, Pageable pageable) throws CompetitionNotFoundException {
        return competitionService.findParticipationRequestsByCompetitionId(competitionId, pageable);
    }

    @GetMapping("{competitionId}/participation-requests/contains/{userId}")
    public ResponseEntity<Boolean> checkIfRequestedParticipation(@PathVariable Long competitionId, @PathVariable Long userId) {
        boolean requestedParticipation = competitionService.checkIfRequestedParticipation(competitionId, userId);
        return ResponseEntity.ok(requestedParticipation);
    }

    @PutMapping("/{competitionId}/init")
    public ResponseEntity<?> initCompetition(@PathVariable Long competitionId) throws CompetitionNotFoundException, CompetitionCannotBeFinishedException {
            competitionService.initCompetition(competitionId);
        return response(HttpStatus.OK, "Competition started successfully");
    }

    @PutMapping("/{competitionId}/finish")
    public ResponseEntity<?> finishCompetition(@PathVariable Long competitionId) throws CompetitionNotFoundException, CompetitionCannotBeFinishedException {
        competitionService.finishCompetition(competitionId);
        return response(HttpStatus.OK, "Competition finished successfully");
    }

    @GetMapping("{competitionId}/defineWinners")
    public ResponseEntity<?> defineWinners(@PathVariable("competitionId") Long competitionId) throws CompetitionNotFoundException {
        competitionService.defineWinners(competitionId);
        return response(HttpStatus.OK, "Winners defined successfully");
    }

    private ResponseEntity<HttpResponse> response(HttpStatus httpStatus, String message) {
        return new ResponseEntity<>(
                new HttpResponse(httpStatus.value(), httpStatus, httpStatus.getReasonPhrase().toUpperCase(), message),
                httpStatus);
    }
}

