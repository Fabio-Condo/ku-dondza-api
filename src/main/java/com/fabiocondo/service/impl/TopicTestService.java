package com.fabiocondo.service.impl;

import com.fabiocondo.domain.Question;
import com.fabiocondo.domain.Quiz;
import com.fabiocondo.domain.TopicTest;
import com.fabiocondo.dto.QuestionDTO;
import com.fabiocondo.exception.domain.TopicNotFoundException;
import com.fabiocondo.repository.TopicTestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class TopicTestService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    TopicTestRepository topicTestRepository;

    public TopicTestService(TopicTestRepository topicTestRepository) {
        this.topicTestRepository = topicTestRepository;
    }

    public TopicTest findById(Long id) throws TopicNotFoundException {
        logger.info("Getting topic by id: " + id);
        return topicTestRepository.findById(id)
                .orElseThrow(() -> new TopicNotFoundException("No topic test found by id: " + id));
    }

    public TopicTest save(TopicTest topicTest) {
        return topicTestRepository.save(topicTest);
    }

    public TopicTest update(TopicTest topicTest, Long id) throws TopicNotFoundException {
        TopicTest existTopicTest = findById(id);
        BeanUtils.copyProperties(topicTest, existTopicTest, "id", "questions", "submittedQuizzes");
        return topicTestRepository.save(existTopicTest);
    }

    public Page<TopicTest> findAll(Pageable pageable) {
        return topicTestRepository.findAll(pageable);
    }

    public List<TopicTest> findAll() {
        return topicTestRepository.findAll();
    }

    //public List<TopicTest> getBySubjectId(Long subjectId) {
    //    return topicRepository.findBySubjectIdAndEnabledTrueOrderByPositionAsc(subjectId);
    //}

    public void delete(Long id) throws TopicNotFoundException {
        TopicTest existTopicTest = findById(id);
        topicTestRepository.deleteById(id);
    }

    public long getTotal(){
        return topicTestRepository.count();
    }

    public Set<Question> getQuestionsByTopicTestId(Long topicTestId) throws TopicNotFoundException {

        TopicTest topicTest = topicTestRepository.findById(topicTestId)
                .orElseThrow(() -> new TopicNotFoundException("No topic test found by id: " + topicTestId));

        return topicTest.getQuestions();
    }

    //public Optional<Quiz> getQuizByUserAndTopicTest(Long topicTestId, Long userId) {
    //    return topicTestRepository.findUserQuizByTopicTest(topicTestId, userId);
    //}

    //public List<TopicTest> getTopicTestsWithUserQuizzes(Long subjectId, Long userId) {

        // Busca todos os TopicTest da disciplina
    //    List<TopicTest> tests = topicTestRepository.findBySubjectId(subjectId);

    //    for (TopicTest test : tests) {
            // Usa o método correto para 1 TopicTest
    //        Optional<Quiz> userQuiz = topicTestRepository.findUserQuizByTopicTest(test.getId(), userId);

            // Substitui submittedQuizzes por apenas o quiz do usuário atual
    //        test.setSubmittedQuizzes(userQuiz.map(Collections::singleton).orElse(Collections.emptySet()));

    //        System.out.println("Tamanho: " + test.getSubmittedQuizzes().size());

    //    }

    //    return tests;
    //}

}
