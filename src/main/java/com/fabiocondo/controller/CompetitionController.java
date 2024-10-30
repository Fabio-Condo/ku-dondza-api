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

    private ResponseEntity<HttpResponse> response(HttpStatus httpStatus, String message) {
        return new ResponseEntity<>(
                new HttpResponse(httpStatus.value(), httpStatus, httpStatus.getReasonPhrase().toUpperCase(), message),
                httpStatus);
    }
}

