package com.fabiocondo.dtoMapper;

import com.fabiocondo.domain.Question;
import com.fabiocondo.domain.User;
import com.fabiocondo.dto.QuestionDTO;
import com.fabiocondo.exception.domain.QuestionNotFoundException;
import com.fabiocondo.repository.UserRepository;
import com.fabiocondo.service.UserService;
import com.fabiocondo.service.impl.CommentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class QuestionMapper {

    private final CommentService commentService;
    private final UserService userService;
    private final UserRepository userRepository;

    public QuestionMapper(CommentService commentService, UserService userService, UserRepository userRepository) {
        this.commentService = commentService;
        this.userService = userService;
        this.userRepository = userRepository;
    }

    public Question dtoToDomainObject(QuestionDTO questionDTO) {
        Question course = new Question();
        course.setId(questionDTO.getId());
        course.setQuestionId(questionDTO.getQuestionId());
        course.setDifficultyLevel(questionDTO.getDifficultyLevel());
        course.setText(questionDTO.getText());
        course.setTip(questionDTO.getTip());
        course.setSolution(questionDTO.getSolution());
        course.setTimeLimit(questionDTO.getTimeLimit());
        course.setFileName(questionDTO.getFileName());
        course.setUrlFile(questionDTO.getUrlFile());
        course.setTopic(questionDTO.getTopic());
        course.setMathExpressions(questionDTO.getMathExpressions());
        course.setAnswers(questionDTO.getAnswers());
        return course;
    }

    public QuestionDTO domainToDTO(Question question, Long currentUserId) {
        QuestionDTO questionDTO = new QuestionDTO();
        questionDTO.setId(question.getId());
        questionDTO.setQuestionId(question.getQuestionId());
        questionDTO.setDifficultyLevel(question.getDifficultyLevel());
        questionDTO.setText(question.getText());
        questionDTO.setTip(question.getTip());
        questionDTO.setSolution(question.getSolution());
        questionDTO.setTimeLimit(question.getTimeLimit());
        questionDTO.setFileName(question.getFileName());
        questionDTO.setUrlFile(question.getUrlFile());
        questionDTO.setTopic(question.getTopic());
        questionDTO.setMathExpressions(question.getMathExpressions());
        questionDTO.setAnswers(question.getAnswers());

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
        questionDTO.setDifficultyLevel(question.getDifficultyLevel());
        questionDTO.setText(question.getText());
        questionDTO.setTip(question.getTip());
        questionDTO.setSolution(question.getSolution());
        questionDTO.setTimeLimit(question.getTimeLimit());
        questionDTO.setFileName(question.getFileName());
        questionDTO.setUrlFile(question.getUrlFile());
        questionDTO.setTopic(question.getTopic());
        questionDTO.setMathExpressions(question.getMathExpressions());
        questionDTO.setAnswers(question.getAnswers());

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
