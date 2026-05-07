package com.fabiocondo.controller;

import com.fabiocondo.domain.Challenge;
import com.fabiocondo.domain.Question;
import com.fabiocondo.dto.ChallengeDTO;
import com.fabiocondo.dtoMapper.ChallengeMapper;
import com.fabiocondo.exception.domain.ChallengeNotFoundException;
import com.fabiocondo.exception.domain.QuestionNotFoundException;
import com.fabiocondo.exception.domain.TopicNotFoundException;
import com.fabiocondo.repository.filter.ChallengeFilter;
import com.fabiocondo.service.impl.ChallengeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;


@RestController
@RequestMapping("/challenges")
public class ChallengeController {

    private final ChallengeService challengeService;
    private final ChallengeMapper challengeMapper;

    public ChallengeController(ChallengeService challengeService,
                               ChallengeMapper challengeMapper) {
        this.challengeService = challengeService;
        this.challengeMapper = challengeMapper;
    }

    @GetMapping("/filter")
    public Page<ChallengeDTO> filter(ChallengeFilter challengeFilter, Pageable pageable) {
        return challengeMapper.toResponsePage(
                challengeService.filter(challengeFilter, pageable)
        );
    }

    @GetMapping("/{challengeId}")
    public ChallengeDTO getByChallengeId(@PathVariable String challengeId) {
        Challenge challenge = challengeService.findByChallengeId(challengeId);
        return challengeMapper.toResponse(challenge);
    }

    @GetMapping("/{id}/questions")
    public ResponseEntity<Set<Question>> getQuestionsByChallengeId(@PathVariable("id") Long challengeId) throws TopicNotFoundException {
        Set<Question> questions = challengeService.getQuestionsByChallengeId(challengeId);
        return ResponseEntity.ok(questions);
    }

    @PostMapping("/{challengeId}/questions/{questionId}")
    public ResponseEntity<Challenge> addQuestionToChallengeQuestions(@PathVariable Long challengeId, @PathVariable Long questionId) throws QuestionNotFoundException, ChallengeNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(challengeService.addQuestionToChallengeQuestions(challengeId, questionId));
    }

    @DeleteMapping("/{challengeId}/questions/{questionId}")
    public ResponseEntity<Challenge> removeQuestionFromChallengeQuestions(@PathVariable Long challengeId, @PathVariable Long questionId) throws QuestionNotFoundException, ChallengeNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(challengeService.removeQuestionFromChallengeQuestions(challengeId, questionId));
    }
}
