package com.fabiocondo.controller;

import com.fabiocondo.domain.Challenge;
import com.fabiocondo.domain.Question;
import com.fabiocondo.domain.Quiz;
import com.fabiocondo.dto.ChallengeSummaryDTO;
import com.fabiocondo.dto.ChallengeRankingResultDTO;
import com.fabiocondo.dtoMapper.ChallengeMapper;
import com.fabiocondo.exception.domain.ChallengeNotFoundException;
import com.fabiocondo.exception.domain.QuestionNotFoundException;
import com.fabiocondo.exception.domain.TopicNotFoundException;
import com.fabiocondo.repository.filter.ChallengeFilter;
import com.fabiocondo.service.impl.ChallengeService;
import com.fabiocondo.service.impl.QuizService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;


@RestController
@RequestMapping("/challenges")
public class ChallengeController {

    private final ChallengeService challengeService;
    private final ChallengeMapper challengeMapper;
    private final QuizService quizService;

    public ChallengeController(ChallengeService challengeService,
                               ChallengeMapper challengeMapper, QuizService quizService) {
        this.challengeService = challengeService;
        this.challengeMapper = challengeMapper;
        this.quizService = quizService;
    }

    @GetMapping("/filter")
    public Page<ChallengeSummaryDTO> filter(ChallengeFilter challengeFilter, Pageable pageable) {
        return challengeMapper.toResponsePage(
                challengeService.filter(challengeFilter, pageable)
        );
    }

    @GetMapping("/{challengeId}")
    @Cacheable(value = "challengeSummary", key = "#challengeId")
    public ChallengeSummaryDTO getByChallengeId(@PathVariable String challengeId) throws ChallengeNotFoundException {
        Challenge challenge = challengeService.findByChallengeId(challengeId);
        return challengeMapper.toResponse(challenge);
    }

    @PostMapping
    public ResponseEntity<Challenge> save(@RequestBody Challenge challenge) {
        return ResponseEntity.status(HttpStatus.OK).body(challengeService.save(challenge));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Challenge> update(@PathVariable("id") Long id, @RequestBody Challenge challenge) throws ChallengeNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(challengeService.update(challenge, id));
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

    @GetMapping("/{challengeId}/ranking")
    @Cacheable(value = "ranking", key = "#challengeId")
    public List<ChallengeRankingResultDTO> getRanking(@PathVariable String challengeId)
            throws ChallengeNotFoundException {

        Challenge challenge = challengeService.findByChallengeId(challengeId);

        List<Quiz> quizzes = new ArrayList<>(challenge.getSubmittedChallengeQuizzes());

        quizzes.sort(Comparator
                .comparingDouble((Quiz q) -> quizService.calculateAccuracyRate(q))
                .reversed()
                .thenComparing(Quiz::getSubmittedAt) // mais antigo primeiro
                .thenComparing(Quiz::getId) // critério de desempate adicional
        );

        List<ChallengeRankingResultDTO> ranking = new ArrayList<>();
        int position = 1;

        for (Quiz quiz : quizzes) {
            ranking.add(challengeMapper.toRankingResponse(quiz, position++));
        }

        return ranking;
    }
}
