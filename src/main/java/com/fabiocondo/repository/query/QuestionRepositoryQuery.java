package com.fabiocondo.repository.query;

import com.fabiocondo.domain.Question;
import com.fabiocondo.enumeration.DifficultyLevel;
import com.fabiocondo.repository.filter.QuestionFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Set;

public interface QuestionRepositoryQuery {
    public Page<Question> filter(QuestionFilter questionFilter, Pageable pageable);

    Set<Question> findRandomQuestionsByTopicsAndDifficulty(Set<Long> topicIds, DifficultyLevel difficultyLevel, int limitPerTopic);
}
