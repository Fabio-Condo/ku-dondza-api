package com.fabiocondo.controller;

import com.fabiocondo.domain.Competition;
import com.fabiocondo.domain.HttpResponse;
import com.fabiocondo.domain.Question;
import com.fabiocondo.domain.User;
import com.fabiocondo.exception.domain.CompetitionNotFoundException;
import com.fabiocondo.exception.domain.QuestionNotFoundException;
import com.fabiocondo.exception.domain.UserNotFoundException;
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
    public ResponseEntity<Competition> createCompetition(@RequestBody Competition competition) {
        return ResponseEntity.status(HttpStatus.OK).body(competitionService.createCompetition(competition));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Competition> updateCompetition(@PathVariable("id") Long id, @RequestBody Competition competition) {
        return ResponseEntity.status(HttpStatus.OK).body(competitionService.updateCompetition(id, competition));
    }

    @GetMapping("/filter")
    public ResponseEntity<Page<Competition>> findAll(Pageable pageable) {
        System.out.println("Page: " + pageable.toString());
        return ResponseEntity.status(HttpStatus.OK).body(competitionService.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Competition> getCompetitionById(@PathVariable Long id) {
        return ResponseEntity.ok().body(competitionService.getCompetitionById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") Long id) {
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
    public ResponseEntity<Competition> addParticipantToCompetition(@PathVariable Long competitionId, @PathVariable Long userId) throws UserNotFoundException {
        Competition updatedCompetition = competitionService.addParticipantToCompetition(competitionId, userId);
        return updatedCompetition != null ? ResponseEntity.ok(updatedCompetition) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{competitionId}/participants/{userId}")
    public ResponseEntity<Competition> removeParticipantFromCompetition(@PathVariable Long competitionId, @PathVariable Long userId) throws UserNotFoundException {
        Competition updatedCompetition = competitionService.removeParticipantFromCompetition(competitionId, userId);
        return updatedCompetition != null ? ResponseEntity.ok(updatedCompetition) : ResponseEntity.notFound().build();
    }

    @GetMapping("/{competitionId}/participants/total")
    public ResponseEntity<Long> countParticipantsByCompetitionId(@PathVariable Long competitionId){
        return ResponseEntity.status(HttpStatus.OK).body(competitionService.countParticipantsByCompetitionId(competitionId));
    }

    @GetMapping("/{competitionId}/questions")
    public Page<Question> getQuestionsByCompetitionId(@PathVariable Long competitionId, Pageable pageable) throws CompetitionNotFoundException {
        return competitionService.getQuestionsByCompetitionId(competitionId, pageable);
    }

    @PostMapping("/{competitionId}/questions/{questionId}")
    public ResponseEntity<Competition> addQuestionToCompetition(@PathVariable Long competitionId, @PathVariable Long questionId) throws QuestionNotFoundException {
        Competition updatedCompetition = competitionService.addQuestionToCompetition(competitionId, questionId);
        return updatedCompetition != null ? ResponseEntity.ok(updatedCompetition) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{competitionId}/questions/{questionId}")
    public ResponseEntity<Competition> removeQuestionFromCompetition(@PathVariable Long competitionId, @PathVariable Long questionId) throws QuestionNotFoundException {
        Competition updatedCompetition = competitionService.removeQuestionFromCompetition(competitionId, questionId);
        return updatedCompetition != null ? ResponseEntity.ok(updatedCompetition) : ResponseEntity.notFound().build();
    }

    @GetMapping("/{competitionId}/questions/total")
    public ResponseEntity<Long> countQuestionsByCompetitionId(@PathVariable Long competitionId){
        return ResponseEntity.status(HttpStatus.OK).body(competitionService.countQuestionsByCompetitionId(competitionId));
    }

    @PostMapping("/{competitionId}/send-participation-request/{userId}")
    public void sendParticipationRequest(@PathVariable Long competitionId, @PathVariable Long userId) throws UserNotFoundException {
        competitionService.sendParticipationRequest(competitionId, userId);
    }

    @PostMapping("/{competitionId}/accept-participation-requests/{userId}")
    public Competition acceptParticipationRequest(@PathVariable Long competitionId, @PathVariable Long userId) throws UserNotFoundException {
        return competitionService.acceptParticipationRequest(competitionId, userId);
    }

    @DeleteMapping("/{competitionId}/reject-participation-requests/{userId}")
    public void rejectParticipationRequest(@PathVariable Long competitionId, @PathVariable Long userId) throws UserNotFoundException {
        competitionService.rejectParticipationRequest(competitionId, userId);
    }

    @GetMapping("/{competitionId}/participation-requests")
    public Page<User> findParticipationRequestsByCompetitionId(@PathVariable Long competitionId, Pageable pageable) throws CompetitionNotFoundException {
        return competitionService.findParticipationRequestsByCompetitionId(competitionId, pageable);
    }

    //@GetMapping("/{competitionId}/participation-requests")
    public ResponseEntity<Set<User>> getFriendRequests(@PathVariable Long competitionId) {
        return ResponseEntity.status(HttpStatus.OK).body(competitionService.getParticipationRequest(competitionId));
    }

    private ResponseEntity<HttpResponse> response(HttpStatus httpStatus, String message) {
        return new ResponseEntity<>(
                new HttpResponse(httpStatus.value(), httpStatus, httpStatus.getReasonPhrase().toUpperCase(), message),
                httpStatus);
    }
}

