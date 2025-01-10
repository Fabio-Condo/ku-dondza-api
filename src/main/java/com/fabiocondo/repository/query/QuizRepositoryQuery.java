package com.fabiocondo.repository.query;

import com.fabiocondo.domain.Quiz;
import com.fabiocondo.repository.filter.QuizFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface QuizRepositoryQuery {
    public Page<Quiz> filter(QuizFilter quizFilter, Pageable pageable);
}
