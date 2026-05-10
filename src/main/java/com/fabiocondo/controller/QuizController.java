package com.fabiocondo.controller;

import com.fabiocondo.domain.*;
import com.fabiocondo.dto.QuizDTO;
import com.fabiocondo.dtoMapper.QuizMapper;
import com.fabiocondo.exception.domain.*;
import com.fabiocondo.repository.filter.QuizFilter;
import com.fabiocondo.service.impl.ChallengeService;
import com.fabiocondo.service.impl.QuizService;
import com.fabiocondo.service.impl.TestService;
import com.fabiocondo.service.impl.UserSubjectScoreService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/quizzes")
public class QuizController {

    private final QuizService quizService;

    private final QuizMapper quizMapper;

    private final TestService testService;

    private final ChallengeService challengeService;


    private final UserSubjectScoreService userSubjectScoreService;

    public QuizController(QuizService quizService, QuizMapper quizMapper, TestService testService, ChallengeService challengeService, UserSubjectScoreService userSubjectScoreService) {
        this.quizService = quizService;
        this.quizMapper = quizMapper;
        this.testService = testService;
        this.challengeService = challengeService;
        this.userSubjectScoreService = userSubjectScoreService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Quiz> findById(@PathVariable("id") Long id) throws QuizNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(quizService.findById(id));
    }

    @GetMapping("/find-by-quizId/{quizId}")
    public ResponseEntity<QuizDTO> findQuizByQuizId(@PathVariable("quizId") String quizId, @RequestParam("currentUserId") Long currentUserId) throws QuizNotFoundException {
        Quiz quiz = quizService.findQuizByQuizId(quizId);
        return ResponseEntity.status(HttpStatus.OK).body(quizMapper.domainToDTO_WithQuestionsAndAnswers(quiz, currentUserId));
    }

    @GetMapping("/filter")
    public Page<QuizDTO> filter(QuizFilter quizFilter, Pageable pageable) {
        return quizMapper.domainPageToDTOPage(quizService.filter(quizFilter, pageable), pageable);
    }

    @GetMapping("/by-question/{questionId}")
    public Page<Quiz> getQuizzesByQuestionId(@PathVariable("questionId") Long questionId, Pageable pageable) throws QuizNotFoundException {
        return quizService.getQuizzesByQuestionId(questionId, pageable);
    }

    @PostMapping
    public ResponseEntity<QuizDTO> createQuiz(@RequestBody Quiz quiz,
                                               @RequestParam Set<Long> questionIds,
                                               @RequestParam Set<Long> userAnswerIds,
                                               @RequestParam("currentUserId") Long currentUserId) {

        Quiz savedQuiz = quizService.saveQuizWithQuestions(quiz, questionIds, userAnswerIds);
        return ResponseEntity.status(HttpStatus.OK).body(quizMapper.domainToDTO_WithQuestionsAndAnswers(savedQuiz, currentUserId));
    }

    @PostMapping("/topic-test")
    public ResponseEntity<QuizDTO> saveQuizTopicTest(
            @RequestBody Quiz quiz,
            @RequestParam Set<Long> questionIds,
            @RequestParam Set<Long> userAnswerIds,
            @RequestParam("topicTestId") Long topicTestId,
            @RequestParam("currentUserId") Long currentUserId
    ) throws TopicNotFoundException, UserAlreadySubmittedException {

        // Valida se o utilizador já submeteu o teste
        testService.validateUserHasNotSubmittedQuiz(topicTestId, currentUserId);

        // Salva quiz com perguntas e respostas
        Quiz savedQuiz = quizService.saveQuizWithQuestions(
                quiz,
                questionIds,
                userAnswerIds
        );

        QuizDTO quizDTO = quizMapper.domainToDTO(savedQuiz);

        // Regra: aprovado com 80% ou mais
        if (quizDTO.getAccuracyRate() >= 80.0) {

            Test test = testService.findById(topicTestId);

            test.getSubmittedQuizzes().add(savedQuiz);
            testService.save(test);

            User user = savedQuiz.getUser();
            Subject subject = savedQuiz.getSubject();

            // quantidade de respostas corretas
            int correctAnswers = quizService.countCorrectAnswers(savedQuiz);

            // cada acerto vale 10 pontos
            long points = correctAnswers * 10L;

            userSubjectScoreService.addScore(user, subject, points);
        }

        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        quizMapper.domainToDTO_WithQuestionsAndAnswers(
                                savedQuiz,
                                currentUserId
                        )
                );
    }

    @PostMapping("/challenge")
    public ResponseEntity<QuizDTO> saveQuizChallenge(
            @RequestBody Quiz quiz,
            @RequestParam Set<Long> questionIds,
            @RequestParam Set<Long> userAnswerIds,
            @RequestParam("challengeId") Long challengeId,
            @RequestParam("currentUserId") Long currentUserId
    ) throws UserAlreadySubmittedException,
            ChallengeNotFoundException,
            ChallengeUnavailableException {

        // valida se challenge ainda pode ser realizado
        challengeService.validateChallengeAvailability(challengeId);

        // valida se utilizador já submeteu
        challengeService.validateUserHasNotSubmittedQuiz(challengeId, currentUserId);

        // salva quiz
        Quiz savedQuiz = quizService.saveQuizWithQuestions(
                quiz,
                questionIds,
                userAnswerIds
        );

        // associa quiz ao challenge
        Challenge challenge = challengeService.findById(challengeId);
        challenge.getSubmittedChallengeQuizzes().add(savedQuiz);

        challengeService.save(challenge);

        return ResponseEntity.ok(
                quizMapper.domainToDTO_WithQuestionsAndAnswers(
                        savedQuiz,
                        currentUserId
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") Long id) throws QuizNotFoundException {
        quizService.delete(id);
        return response(HttpStatus.OK, "Quiz deleted successfully");
    }

    @PutMapping("/{id}/anonymous")
    public void toggleAnonymous(@PathVariable("id") Long id, @RequestBody Boolean status) throws QuizNotFoundException {
        quizService.toggleAnonymous(id, status);
    }

    @GetMapping("/{quizId}/questions/total")
    public ResponseEntity<Long> countQuestionsByQuizId(@PathVariable Long quizId){
        return ResponseEntity.status(HttpStatus.OK).body(quizService.countQuestionsByQuizId(quizId));
    }

    private ResponseEntity<HttpResponse> response(HttpStatus httpStatus, String message) {
        return new ResponseEntity<>(
                new HttpResponse(httpStatus.value(), httpStatus, httpStatus.getReasonPhrase().toUpperCase(), message),
                httpStatus);
    }

}
