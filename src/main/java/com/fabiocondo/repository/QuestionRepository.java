package com.fabiocondo.repository;

import com.fabiocondo.domain.Question;
import com.fabiocondo.repository.query.QuestionRepositoryQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface QuestionRepository extends JpaRepository<Question, Long>, QuestionRepositoryQuery {
    @Query("SELECT q FROM Question q WHERE q.text LIKE %:searchParam%")
    public Page<Question> findAll(@Param("searchParam") String searchParam, Pageable pageable);
}
