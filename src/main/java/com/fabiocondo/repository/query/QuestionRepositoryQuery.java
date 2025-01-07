package com.fabiocondo.repository.query;

import com.fabiocondo.domain.Question;
import com.fabiocondo.repository.filter.QuestionFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface QuestionRepositoryQuery {
    public Page<Question> filter(QuestionFilter questionFilter, Pageable pageable);
}
