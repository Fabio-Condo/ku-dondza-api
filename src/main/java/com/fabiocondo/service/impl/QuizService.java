package com.fabiocondo.service.impl;

import com.fabiocondo.constant.CacheNames;
import com.fabiocondo.domain.*;
import com.fabiocondo.dto.*;
import com.fabiocondo.dtoMapper.QuestionMapper;
import com.fabiocondo.exception.domain.QuizNotFoundException;
import com.fabiocondo.repository.AnswerRepository;
import com.fabiocondo.repository.QuestionRepository;
import com.fabiocondo.repository.QuizRepository;
import com.fabiocondo.repository.UserRepository;
import com.fabiocondo.repository.filter.QuizFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class QuizService {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final QuestionMapper questionMapper;
    private final UserRepository userRepository;
    private final UserServiceImpl userService;


    public QuizService(QuizRepository quizRepository, QuestionRepository questionRepository, AnswerRepository answerRepository, QuestionMapper questionMapper, UserRepository userRepository, UserServiceImpl userService) {
        this.quizRepository = quizRepository;
        this.questionRepository = questionRepository;
        this.answerRepository = answerRepository;
        this.questionMapper = questionMapper;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    public Quiz findById(Long id) throws QuizNotFoundException {
        logger.info("Getting quiz by id: " + id);
        return quizRepository.findById(id)
                .orElseThrow(() -> new QuizNotFoundException("No quiz found by id: " + id));
    }

    public Quiz findQuizByQuizId(String quizId) throws QuizNotFoundException {
        return quizRepository.findQuizByQuizId(quizId)
                .orElseThrow(() -> new QuizNotFoundException("No quiz found by id: " + quizId));
    }

    // Método modificado para usar JOIN FETCH e ser cache-safe
    @Cacheable(
            value = CacheNames.QUIZ_DETAILS,
            key = "#quizId + '-' + #currentUserId"
    )
    public QuizDTO getQuizWithDetails(String quizId, Long currentUserId) throws QuizNotFoundException {
        Quiz quiz = quizRepository.findQuizByQuizId(quizId)
                .orElseThrow(() -> new QuizNotFoundException("No quiz found by id: " + quizId));
        return domainToDTO_WithQuestionsAndAnswers(quiz, currentUserId);
    }

    public Page<Quiz> filter(QuizFilter quizFilter, Pageable pageable) {
        return quizRepository.filter(quizFilter, pageable);
    }

    @Cacheable(
            value = CacheNames.QUIZ_FILTER,
            key =
                    "#quizFilter.searchParam + '-' +" +
                            "#quizFilter.subject + '-' +" +
                            "#quizFilter.user + '-' +" +
                            "#pageable.pageNumber + '-' +" +
                            "#pageable.pageSize + '-' +" +
                            "#pageable.sort.toString()"
    )
    public PageResponse<QuizDTO> filterWithCash(QuizFilter quizFilter, Pageable pageable) {
        Page<Quiz> page = quizRepository.filter(quizFilter, pageable);

        // Converte Quiz para QuizDTO
        List<QuizDTO> content = page.getContent().stream()
                .map(this::domainToDTO)
                .collect(Collectors.toList());

        return new PageResponse<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements()
        );
    }

    public Page<Quiz> getQuizzesByQuestionId(Long questionId, Pageable pageable) throws QuizNotFoundException {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new QuizNotFoundException("No question found by id: " + questionId));
        return quizRepository.findAllByQuestions(question, pageable);
    }

    @CacheEvict(
            value = {
                    CacheNames.QUIZ_FILTER,
                    CacheNames.QUIZ_DETAILS
            },
            allEntries = true
    )
    @Transactional
    public Quiz saveQuizWithQuestions(Quiz quiz, Set<Long> questionIds, Set<Long> userAnswerIds) {

        if (quiz == null) {
            throw new IllegalArgumentException("O objeto Quiz não pode ser nulo.");
        }

        if (questionIds == null || questionIds.isEmpty()) {
            throw new IllegalArgumentException("O Quiz deve ter pelo menos uma questão associada.");
        }

        Set<Question> questions = new HashSet<>(questionRepository.findAllById(questionIds));
        Set<Answer> answers = new HashSet<>(answerRepository.findAllById(userAnswerIds));

        if (questions.size() != questionIds.size()) {
            throw new IllegalArgumentException("Uma ou mais questões não foram encontradas no banco de dados.");
        }

        quiz.setQuizId(UUID.randomUUID().toString());
        quiz.setSubmittedAt(new Date());
        quiz.setQuestions(questions);
        quiz.setAnswers(answers);

        return quizRepository.save(quiz);
    }

    public void delete(Long id) throws QuizNotFoundException {
        Quiz existQuiz = findById(id);
        quizRepository.deleteById(id);
    }

    public long countByUserId(Long userId){
        return quizRepository.countByUserId(userId);
    }

    public long countQuestionsByQuizId(Long quizId){
        return quizRepository.countQuestionsByQuizId(quizId);
    }

    public Set<Topic> getTopics(Quiz quiz) {
        Set<Topic> topics = new HashSet<>();
        quiz.getQuestions().forEach(question -> {
            if (question.getTopic() != null) {
                topics.add(question.getTopic());
            }
        });
        return topics;
    }

    public Set<Topic> getSortedTopics(Quiz quiz) {
        Set<Topic> topics = new HashSet<>();
        for (Question question : quiz.getQuestions()) {
            if (question.getTopic() != null) {
                topics.add(question.getTopic());
            }
        }

        List<Topic> sortedTopics = new ArrayList<>(topics);
        Collections.sort(sortedTopics, Comparator.comparing(Topic::getName)); // ou getId, etc.

        return new LinkedHashSet<>(sortedTopics); // mantém a ordem após a ordenação
    }

    public double calculateAccuracyRate(Quiz quiz) {
        Set<Question> questions = quiz.getQuestions();

        if (questions.isEmpty()) {
            return 0.0;
        }

        int correctAnswers = countCorrectAnswers(quiz);

        return (double) correctAnswers / questions.size() * 100;
    }

    public int countCorrectAnswers(Quiz quiz) {
        Set<Question> questions = quiz.getQuestions();
        Set<Answer> userAnswers = quiz.getAnswers();

        int correctAnswers = 0;

        for (Question question : questions) {
            for (Answer userAnswer : userAnswers) {
                if (userAnswer.getQuestion().equals(question) && userAnswer.isCorrect()) {
                    correctAnswers++;
                    break;
                }
            }
        }

        return correctAnswers;
    }

    public void toggleAnonymous(Long id, Boolean status) throws QuizNotFoundException {
        Quiz quiz = findById(id);
        quiz.setAnonymous(status);
        quizRepository.save(quiz);
    }

    private QuizDTO domainToDTO(Quiz quiz) {
        QuizDTO quizDTO = new QuizDTO();
        quizDTO.setId(quiz.getId());
        quizDTO.setQuizId(quiz.getQuizId());
        quizDTO.setType(quiz.getType());
        quizDTO.setLimitPerTopic(quiz.getLimitPerTopic());
        quizDTO.setSubmittedAt(quiz.getSubmittedAt());
        quizDTO.setTimeLimit(quiz.getTimeLimit());
        quizDTO.setTimeSpent(quiz.getTimeSpent());
        quizDTO.setAnonymous(quiz.isAnonymous());

        // Subject - converta para DTO
        if (quiz.getSubject() != null) {
            SubjectDto subjectDTO = new SubjectDto();
            subjectDTO.setId(quiz.getSubject().getId());
            subjectDTO.setName(quiz.getSubject().getName());
            subjectDTO.setDescription(quiz.getSubject().getDescription());
            subjectDTO.setCategory(quiz.getSubject().getCategory());
            quizDTO.setSubject(subjectDTO);
        }

        // Topics - agora as questions já foram carregadas pelo JOIN FETCH
        Set<TopicDTO> topicDTOs = getSortedTopicsSafe(quiz);
        quizDTO.setTopics(new HashSet<>(topicDTOs));

        quizDTO.setTotalQuestions(quizRepository.countQuestionsByQuizId(quiz.getId()));
        quizDTO.setAccuracyRate(calculateAccuracyRate(quiz));

        // User
        if (quiz.getUser() != null) {
            UserDTO userDTO = new UserDTO();
            userDTO.setId(quiz.getUser().getId());
            userDTO.setUserId(quiz.getUser().getUserId());
            userDTO.setFullName(quiz.getUser().getFullName());
            userDTO.setEmail(quiz.getUser().getEmail());
            userDTO.setProfileImageUrl(quiz.getUser().getProfileImageUrl());
            quizDTO.setUser(userDTO);
        }

        return quizDTO;
    }

    // Método seguro para pegar topics (agora com dados carregados)
    public Set<TopicDTO> getSortedTopicsSafe(Quiz quiz) {
        Set<TopicDTO> topicDTOs = new HashSet<>();

        if (quiz.getQuestions() != null) {
            for (Question question : quiz.getQuestions()) {
                if (question.getTopic() != null) {
                    Topic topic = question.getTopic();
                    TopicDTO topicDTO = new TopicDTO();
                    topicDTO.setId(topic.getId());
                    topicDTO.setName(topic.getName());
                    topicDTO.setDescription(topic.getDescription());
                    topicDTO.setPosition(topic.getPosition());
                    topicDTOs.add(topicDTO);
                }
            }
        }

        // Converte para List, ordena e remove duplicatas
        List<TopicDTO> sortedList = new ArrayList<>(
                topicDTOs.stream()
                        .collect(Collectors.toMap(
                                TopicDTO::getId,  // Chave = ID
                                dto -> dto,        // Valor = próprio DTO
                                (dto1, dto2) -> dto1  // Em caso de duplicata, mantém o primeiro
                        ))
                        .values()
        );

        sortedList.sort(Comparator.comparing(TopicDTO::getName));

        return new LinkedHashSet<>(sortedList);
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
        //quizDTO.setAnswers(quiz.getAnswers());

        // Subject - converta para DTO
        if (quiz.getSubject() != null) {
            SubjectDto subjectDTO = new SubjectDto();
            subjectDTO.setId(quiz.getSubject().getId());
            subjectDTO.setName(quiz.getSubject().getName());
            subjectDTO.setDescription(quiz.getSubject().getDescription());
            subjectDTO.setCategory(quiz.getSubject().getCategory());
            quizDTO.setSubject(subjectDTO);
        }

        // User
        if (quiz.getUser() != null) {
            UserDTO userDTO = new UserDTO();
            userDTO.setId(quiz.getUser().getId());
            userDTO.setUserId(quiz.getUser().getUserId());
            userDTO.setFullName(quiz.getUser().getFullName());
            userDTO.setEmail(quiz.getUser().getEmail());
            userDTO.setProfileImageUrl(quiz.getUser().getProfileImageUrl());
            quizDTO.setUser(userDTO);
        }

        // CONVERTER Answers para AnswerDTO com QuestionDTO
        if (quiz.getAnswers() != null) {
            Set<AnswerDTO> answerDTOs = quiz.getAnswers().stream()
                    .map(answer -> {
                        AnswerDTO answerDTO = new AnswerDTO();
                        answerDTO.setId(answer.getId());
                        answerDTO.setText(answer.getText());
                        answerDTO.setCorrect(answer.isCorrect());

                        // Converter Question para QuestionDTO (seguro)
                        if (answer.getQuestion() != null) {
                            QuestionDTO questionDTO = new QuestionDTO();
                            questionDTO.setId(answer.getQuestion().getId());
                            questionDTO.setQuestionId(answer.getQuestion().getQuestionId());
                            questionDTO.setText(answer.getQuestion().getText());
                            questionDTO.setTip(answer.getQuestion().getTip());
                            questionDTO.setSolution(answer.getQuestion().getSolution());
                            questionDTO.setDifficultyLevel(answer.getQuestion().getDifficultyLevel());
                            questionDTO.setTimeLimit(answer.getQuestion().getTimeLimit());
                            questionDTO.setValidated(answer.getQuestion().isValidated());
                            // NÃO inclua answers aqui para evitar ciclo infinito
                            // questionDTO.setAnswers(null);

                            answerDTO.setQuestion(questionDTO);
                        }

                        return answerDTO;
                    })
                    .collect(Collectors.toSet());
            quizDTO.setAnswers(answerDTOs);
        }

        Optional<User> currentUser = userRepository.findById(currentUserId);

        quizDTO.setQuestions(sortQuestionsByTopicPositionAndId(quiz.getQuestions(), currentUser));

        return quizDTO;
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

}
