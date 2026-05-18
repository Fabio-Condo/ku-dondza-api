package com.fabiocondo.dtoMapper;

import com.fabiocondo.domain.*;
import com.fabiocondo.dto.*;
import com.fabiocondo.repository.UserRepository;
import com.fabiocondo.service.UserService;
import com.fabiocondo.service.impl.CommentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class QuestionMapper {

    private final CommentService commentService;
    private final UserService userService;
    private final UserRepository userRepository;

    public QuestionMapper(CommentService commentService, UserService userService, UserRepository userRepository ) {
        this.commentService = commentService;
        this.userService = userService;
        this.userRepository = userRepository;
    }

    public Question dtoToDomainObject(QuestionDTO questionDTO) {
        Question question = new Question();
        question.setId(questionDTO.getId());
        question.setQuestionId(questionDTO.getQuestionId());
        question.setText(questionDTO.getText());
        question.setTip(questionDTO.getTip());
        question.setSolution(questionDTO.getSolution());
        question.setDifficultyLevel(questionDTO.getDifficultyLevel());
        question.setTimeLimit(questionDTO.getTimeLimit());
        question.setValidated(questionDTO.isValidated());
        question.setFileName(questionDTO.getFileName());
        question.setUrlFile(questionDTO.getUrlFile());

        // Converter TopicDTO para Topic (entidade)
        if (questionDTO.getTopic() != null) {
            Topic topic = new Topic();
            topic.setId(questionDTO.getTopic().getId());
            topic.setName(questionDTO.getTopic().getName());
            topic.setDescription(questionDTO.getTopic().getDescription());
            topic.setPosition(questionDTO.getTopic().getPosition());
            question.setTopic(topic);
        }

        // Converter MathExpressionDTO para MathExpression (entidade)
        if (questionDTO.getMathExpressions() != null) {
            List<MathExpression> mathExpressions = questionDTO.getMathExpressions().stream()
                    .map(meDTO -> {
                        MathExpression mathExpression = new MathExpression();
                        mathExpression.setId(meDTO.getId());
                        mathExpression.setExpression(meDTO.getExpression());
                        mathExpression.setName(meDTO.getName());
                        mathExpression.setQuestion(question); // Seta a referência de volta
                        return mathExpression;
                    })
                    .collect(Collectors.toList());
            question.setMathExpressions(mathExpressions);
        }

        // Converter AnswerDTO para Answer (entidade)
        if (questionDTO.getAnswers() != null) {
            List<Answer> answers = questionDTO.getAnswers().stream()
                    .map(answerDTO -> {
                        Answer answer = new Answer();
                        answer.setId(answerDTO.getId());
                        answer.setText(answerDTO.getText());
                        answer.setCorrect(answerDTO.isCorrect());
                        answer.setQuestion(question); // Seta a referência de volta
                        return answer;
                    })
                    .collect(Collectors.toList());
            question.setAnswers(answers);
        }

        return question;
    }

    public QuestionDTO domainToDTO(Question question, Long currentUserId) {
        QuestionDTO questionDTO = new QuestionDTO();
        questionDTO.setId(question.getId());
        questionDTO.setQuestionId(question.getQuestionId());
        questionDTO.setText(question.getText());
        questionDTO.setTip(question.getTip());
        questionDTO.setSolution(question.getSolution());
        questionDTO.setDifficultyLevel(question.getDifficultyLevel());
        questionDTO.setTimeLimit(question.getTimeLimit());
        questionDTO.setValidated(question.isValidated());
        questionDTO.setFileName(question.getFileName());
        questionDTO.setUrlFile(question.getUrlFile());

        if (question.getTopic() != null) {
            TopicDTO topicDTO = new TopicDTO();
            topicDTO.setId(question.getTopic().getId());
            topicDTO.setName(question.getTopic().getName());

            // CONVERTER Subject para SubjectDTO
            if (question.getTopic().getSubject() != null) {
                SubjectDto subjectDTO = new SubjectDto();
                subjectDTO.setId(question.getTopic().getSubject().getId());
                subjectDTO.setName(question.getTopic().getSubject().getName());
                subjectDTO.setDescription(question.getTopic().getSubject().getDescription());
                subjectDTO.setCategory(question.getTopic().getSubject().getCategory());
                topicDTO.setSubject(subjectDTO);
            }

            topicDTO.setDescription(question.getTopic().getDescription());
            topicDTO.setPosition(question.getTopic().getPosition());
            questionDTO.setTopic(topicDTO);
        }

        //questionDTO.setMathExpressions(question.getMathExpressions());

        if (question.getMathExpressions() != null) {
            List<MathExpressionDTO> mathExpressionDTOs = question.getMathExpressions().stream()
                    .map(me -> {
                        MathExpressionDTO meDTO = new MathExpressionDTO();
                        meDTO.setId(me.getId());
                        meDTO.setName(me.getName());
                        meDTO.setExpression(me.getExpression());
                        //meDTO.setQuestion(me.getQuestion()); Recebe DTO

                        return meDTO;
                    })
                    .collect(Collectors.toList());
            questionDTO.setMathExpressions(mathExpressionDTOs);
        }

        //questionDTO.setAnswers(question.getAnswers());

        // CONVERTER Answers para AnswerDTO (NÃO usar entidades diretamente)
        if (question.getAnswers() != null) {
            List<AnswerDTO> answerDTOs = question.getAnswers().stream()
                    .map(answer -> {
                        AnswerDTO answerDTO = new AnswerDTO();
                        answerDTO.setId(answer.getId());
                        answerDTO.setText(answer.getText());
                        answerDTO.setCorrect(answer.isCorrect());
                        return answerDTO;
                    })
                    .collect(Collectors.toList());
            questionDTO.setAnswers(answerDTOs);
        }

        Optional<User> currentUser = userRepository.findById(currentUserId);

        if(currentUser.isPresent()){
            questionDTO.setSavedByUser(userService.checkIfSavedQuestion(question.getId(), currentUserId));
        }

        questionDTO.setNumberOfComments(commentService.countCommentsByQuestionId(question.getId()));
        return questionDTO;
    }

    public QuestionDTO domainToDTO(Question question) {
        QuestionDTO questionDTO = new QuestionDTO();
        questionDTO.setId(question.getId());
        questionDTO.setQuestionId(question.getQuestionId());
        questionDTO.setText(question.getText());
        questionDTO.setTip(question.getTip());
        questionDTO.setSolution(question.getSolution());
        questionDTO.setDifficultyLevel(question.getDifficultyLevel());
        questionDTO.setTimeLimit(question.getTimeLimit());
        questionDTO.setValidated(question.isValidated());
        questionDTO.setFileName(question.getFileName());
        questionDTO.setUrlFile(question.getUrlFile());
        //questionDTO.setMathExpressions(question.getMathExpressions());

        if (question.getTopic() != null) {
            TopicDTO topicDTO = new TopicDTO();
            topicDTO.setId(question.getTopic().getId());
            topicDTO.setName(question.getTopic().getName());
            topicDTO.setDescription(question.getTopic().getDescription());
            topicDTO.setPosition(question.getTopic().getPosition());
            // NÃO inclua contents, questions ou outras coleções
            questionDTO.setTopic(topicDTO);
        }

        // MathExpressions - se for entidade, também precisa converter
        if (question.getMathExpressions() != null) {
            List<MathExpressionDTO> mathExpressionDTOs = question.getMathExpressions().stream()
                    .map(me -> {
                        MathExpressionDTO meDTO = new MathExpressionDTO();
                        meDTO.setId(me.getId());
                        meDTO.setName(me.getName());
                        meDTO.setExpression(me.getExpression());
                        //meDTO.setQuestion(me.getQuestion()); Recebe DTO

                        return meDTO;
                    })
                    .collect(Collectors.toList());
            questionDTO.setMathExpressions(mathExpressionDTOs);
        }

        // CONVERTER Answers para AnswerDTO (NÃO usar entidades diretamente)
        if (question.getAnswers() != null) {
            List<AnswerDTO> answerDTOs = question.getAnswers().stream()
                    .map(answer -> {
                        AnswerDTO answerDTO = new AnswerDTO();
                        answerDTO.setId(answer.getId());
                        answerDTO.setText(answer.getText());
                        answerDTO.setCorrect(answer.isCorrect());
                        return answerDTO;
                    })
                    .collect(Collectors.toList());
            questionDTO.setAnswers(answerDTOs);
        }

        questionDTO.setNumberOfComments(commentService.countCommentsByQuestionId(question.getId()));
        return questionDTO;
    }

    public Page<QuestionDTO> domainPageToDTOPage(Page<Question> questions, Long currentUserId, Pageable pageable) {
        return new PageImpl<>(
                questions.stream()
                        .map(question -> domainToDTO(question, currentUserId))
                        .collect(Collectors.toList()),
                pageable,
                questions.getTotalElements()
        );
    }


    public Set<QuestionDTO> domainPageToDTOSet(Set<Question> questions) {
        return questions.stream()
                .map(this::domainToDTO)
                .collect(Collectors.toSet());
    }


}
