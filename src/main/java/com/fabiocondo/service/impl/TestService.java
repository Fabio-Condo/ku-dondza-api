package com.fabiocondo.service.impl;

import com.fabiocondo.domain.*;
import com.fabiocondo.exception.domain.QuestionNotFoundException;
import com.fabiocondo.exception.domain.TopicNotFoundException;
import com.fabiocondo.exception.domain.UserAlreadySubmittedException;
import com.fabiocondo.repository.QuestionRepository;
import com.fabiocondo.repository.TopicTestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class TestService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    TopicTestRepository topicTestRepository;

    QuestionRepository questionRepository;

    public TestService(TopicTestRepository topicTestRepository, QuestionRepository questionRepository) {
        this.topicTestRepository = topicTestRepository;
        this.questionRepository = questionRepository;
    }

    public Test findById(Long id) throws TopicNotFoundException {
        logger.info("Getting test by id: " + id);
        return topicTestRepository.findById(id)
                .orElseThrow(() -> new TopicNotFoundException("No test found by id: " + id));
    }

    public void validateUserHasNotSubmittedQuiz(Long topicTestId, Long userId) throws UserAlreadySubmittedException {

        boolean alreadySubmitted = topicTestRepository
                .existsQuizInTest(topicTestId, userId);

        if (alreadySubmitted) {
            throw new UserAlreadySubmittedException(
                    "Este utilizador já submeteu este teste."
            );
        }
    }

    public Test save(Test test) {
        return topicTestRepository.save(test);
    }

    public Test update(Test test, Long id) throws TopicNotFoundException {
        Test existTest = findById(id);
        BeanUtils.copyProperties(test, existTest, "id", "questions", "submittedQuizzes");
        return topicTestRepository.save(existTest);
    }

    public Page<Test> findAll(Pageable pageable) {
        return topicTestRepository.findAll(pageable);
    }

    public List<Test> findAll() {
        return topicTestRepository.findAll();
    }

    //public List<Test> getBySubjectId(Long subjectId) {
    //    return topicRepository.findBySubjectIdAndEnabledTrueOrderByPositionAsc(subjectId);
    //}

    public void delete(Long id) throws TopicNotFoundException {
        Test existTest = findById(id);
        topicTestRepository.deleteById(id);
    }

    public long getTotal(){
        return topicTestRepository.count();
    }

    public Set<Question> getQuestionsByTestId(Long testId) throws TopicNotFoundException {

        Test test = topicTestRepository.findById(testId)
                .orElseThrow(() -> new TopicNotFoundException("No test found by id: " + testId));

        return test.getQuestions();
    }

    public Test addQuestionToTestQuestions(Long testId, Long questionId) throws TopicNotFoundException, QuestionNotFoundException {
        Test test = findById(testId);
        Optional<Question> question = questionRepository.findById(questionId);
        if (!question.isPresent()){
            throw new QuestionNotFoundException("Question not found by id: " + questionId);
        }
        test.getQuestions().add(question.get());
        return topicTestRepository.save(test);
    }

    public Test removeQuestionFromTestQuestions(Long testId, Long questionId) throws TopicNotFoundException, QuestionNotFoundException {
        Test test = findById(testId);
        Optional<Question> question = questionRepository.findById(questionId);
        if (!question.isPresent()) {
            throw new QuestionNotFoundException("Question not found by id: " + questionId);
        }
        test.getQuestions().remove(question.get());
        return topicTestRepository.save(test);
    }

    //public Optional<Quiz> getQuizByUserAndTopicTest(Long topicTestId, Long userId) {
    //    return topicTestRepository.findUserQuizByTopicTest(topicTestId, userId);
    //}

    //public List<Test> getTopicTestsWithUserQuizzes(Long subjectId, Long userId) {

        // Busca todos os Test da disciplina
    //    List<Test> tests = topicTestRepository.findBySubjectId(subjectId);

    //    for (Test test : tests) {
            // Usa o método correto para 1 Test
    //        Optional<Quiz> userQuiz = topicTestRepository.findUserQuizByTopicTest(test.getId(), userId);

            // Substitui submittedQuizzes por apenas o quiz do usuário atual
    //        test.setSubmittedQuizzes(userQuiz.map(Collections::singleton).orElse(Collections.emptySet()));

    //        System.out.println("Tamanho: " + test.getSubmittedQuizzes().size());

    //    }

    //    return tests;
    //}

}
