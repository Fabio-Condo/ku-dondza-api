package com.fabiocondo.dtoMapper;

import com.fabiocondo.domain.Question;
import com.fabiocondo.domain.Quiz;
import com.fabiocondo.domain.Topic;
import com.fabiocondo.domain.User;
import com.fabiocondo.dto.QuestionDTO;
import com.fabiocondo.dto.QuizDTO;
import com.fabiocondo.repository.QuizRepository;
import com.fabiocondo.repository.UserRepository;
import com.fabiocondo.service.impl.QuizService;
import com.fabiocondo.service.impl.UserServiceImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class QuizMapper {

    private final QuizRepository quizRepository;
    private final QuizService quizService;
    private final QuestionMapper questionMapper;
    private final UserRepository userRepository;
    private final UserServiceImpl userService;


    public QuizMapper(QuizRepository quizRepository, QuizService quizService, QuestionMapper questionMapper, UserRepository userRepository, UserServiceImpl userService) {
        this.quizRepository = quizRepository;
        this.quizService = quizService;
        this.questionMapper = questionMapper;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    public Quiz dtoToDomainObject(QuizDTO quizDTO) {
        Quiz quiz = new Quiz();
        quiz.setQuizId(quizDTO.getQuizId());
        quiz.setType(quizDTO.getType());
        quiz.setLimitPerTopic(quizDTO.getLimitPerTopic());
        quiz.setSubmittedAt(quizDTO.getSubmittedAt());
        quiz.setTimeLimit(quizDTO.getTimeLimit());
        quiz.setTimeSpent(quizDTO.getTimeSpent());
        quiz.setAnonymous(quizDTO.isAnonymous());
        quiz.setSubject(quizDTO.getSubject());
        quiz.setUser(quizDTO.getUser());
        //quiz.setQuestions(quizDTO.getQuestions());
        quiz.setAnswers(quizDTO.getAnswers());
        return quiz;
    }

    public QuizDTO domainToDTO(Quiz quiz) {
        QuizDTO quizDTO = new QuizDTO();
        quizDTO.setId(quiz.getId());
        quizDTO.setQuizId(quiz.getQuizId());
        quizDTO.setType(quiz.getType());
        quizDTO.setLimitPerTopic(quiz.getLimitPerTopic());
        quizDTO.setSubmittedAt(quiz.getSubmittedAt());
        quizDTO.setTimeLimit(quiz.getTimeLimit());
        quizDTO.setTimeSpent(quiz.getTimeSpent());
        quizDTO.setAnonymous(quiz.isAnonymous());
        quizDTO.setSubject(quiz.getSubject());
        quizDTO.setUser(quiz.getUser());
        quizDTO.setTotalQuestions(quizRepository.countQuestionsByQuizId(quiz.getId()));
        quizDTO.setTopics(quizService.getSortedTopics(quiz));
        quizDTO.setAccuracyRate(quizService.calculateAccuracyRate(quiz));
        return quizDTO;
    }

    public QuizDTO domainToDTO_WithQuestionsAndAnswers(Quiz quiz, Long currentUserId) {
        QuizDTO quizDTO = new QuizDTO();
        quizDTO.setId(quiz.getId());
        quizDTO.setQuizId(quiz.getQuizId());
        quizDTO.setType(quiz.getType());
        quizDTO.setLimitPerTopic(quiz.getLimitPerTopic());
        quizDTO.setSubmittedAt(quiz.getSubmittedAt());
        quizDTO.setTimeLimit(quiz.getTimeLimit());
        quizDTO.setTimeSpent(quiz.getTimeSpent());
        quizDTO.setAnonymous(quiz.isAnonymous());
        quizDTO.setSubject(quiz.getSubject());
        quizDTO.setUser(quiz.getUser());
        quizDTO.setAnswers(quiz.getAnswers());

        Optional<User> currentUser = userRepository.findById(currentUserId);

        quizDTO.setQuestions(sortQuestionsByTopicPositionAndId(quiz.getQuestions(), currentUser));


        return quizDTO;
    }

    public List<Topic> getTopics(QuizDTO quiz) {
        return quiz.getTopics()
                .stream()
                .sorted(Comparator.comparing(Topic::getName)) // ou getOrder(), getId(), etc.
                .collect(Collectors.toList());
    }

    public Set<QuestionDTO> sortQuestionsByTopicPositionAndId(Set<Question> questions, Optional<User> optionalUser) {

        return questions.stream()
                .sorted(
                        Comparator
                                .comparing((Question q) -> q.getTopic().getPosition())
                                .thenComparing(Question::getId)
                )
                .map(question -> {
                    QuestionDTO questionDTO = questionMapper.domainToDTO(question);

                    optionalUser.ifPresent(user ->
                            questionDTO.setSavedByUser(
                                    userService.checkIfSavedQuestion(question.getId(), user.getId())
                            )
                    );

                    // Se não tiver user, define false (opcional, pode deixar nulo se preferires)
                    if (!optionalUser.isPresent()) {
                        questionDTO.setSavedByUser(false);
                    }

                    return questionDTO;
                })
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public Set<QuestionDTO> sortQuestionsByTopicPositionAndId(Set<Question> questions) {
        return questions.stream()
                .sorted(
                        Comparator
                                .comparing((Question q) -> q.getTopic().getPosition())
                                .thenComparing(Question::getId)
                )
                .map(questionMapper::domainToDTO)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public Page<QuizDTO> domainPageToDTOPage(Page<Quiz> quizzes, Pageable pageable) {
        return new PageImpl<>(quizzes.stream()
                .map(this::domainToDTO)
                .collect(Collectors.toList()), pageable, quizzes.getTotalElements());
    }
}

