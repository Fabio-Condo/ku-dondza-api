package com.fabiocondo.repository.query;

import com.fabiocondo.domain.Exam;
import com.fabiocondo.repository.filter.ExamFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ExamRepositoryQuery {
    public Page<Exam> filter(ExamFilter examFilter, Pageable pageable);
}
