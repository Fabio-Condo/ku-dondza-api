package com.fabiocondo.controller;

import com.fabiocondo.domain.*;
import com.fabiocondo.dto.CompetitionDto;
import com.fabiocondo.dtoMapper.CompetitionMapper;
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
    private final CompetitionMapper competitionMapper;

    public CompetitionController(CompetitionService competitionService, CompetitionMapper competitionMapper) {
        this.competitionService = competitionService;
        this.competitionMapper = competitionMapper;
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
    public Page<CompetitionDto> filter(CompetitionFilter competitionFilter, @RequestParam("currentUserId") Long currentUserId, Pageable pageable) {
        return competitionMapper.domainPageToDTOPage(competitionService.filter(competitionFilter, pageable), currentUserId, pageable);
    }

    @GetMapping("/findAll")
    public ResponseEntity<Page<Competition>> findAll(@RequestParam(required = false, defaultValue = "") String searchParam, Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(competitionService.findAll(searchParam, pageable));
    }

    @GetMapping("/by-question/{questionId}")
    public Page<Competition> getCompetitionsByQuestionId(@PathVariable("questionId") Long questionId, Pageable pageable) throws QuizNotFoundException {
        return competitionService.getCompetitionsByQuestionId(questionId, pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Competition> getCompetitionById(@PathVariable Long id) throws CompetitionNotFoundException {
        return ResponseEntity.ok().body(competitionService.getCompetitionById(id));
    }

    @GetMapping("/find-by-competitionId/{competitionId}")
    public ResponseEntity<CompetitionDto> findCompetitionByCompetitionId(@PathVariable("competitionId") String competitionId, @RequestParam("currentUserId") Long currentUserId) throws CompetitionNotFoundException {
        Competition competition = competitionService.findCompetitionByCompetitionId(competitionId);
        return ResponseEntity.status(HttpStatus.OK).body(competitionMapper.domainToDTO(competition, currentUserId));

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

    private ResponseEntity<HttpResponse> response(HttpStatus httpStatus, String message) {
        return new ResponseEntity<>(
                new HttpResponse(httpStatus.value(), httpStatus, httpStatus.getReasonPhrase().toUpperCase(), message),
                httpStatus);
    }
}

