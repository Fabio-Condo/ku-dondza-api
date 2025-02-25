package com.fabiocondo.repository;

import com.fabiocondo.domain.PeriodicQuiz;
import com.fabiocondo.domain.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PeriodicQuizRepository extends JpaRepository<PeriodicQuiz, Long> {
    @Query("SELECT c FROM PeriodicQuiz c WHERE c.title LIKE %:searchParam%")
    public Page<PeriodicQuiz> findAll(@Param("searchParam") String searchParam, Pageable pageable);

    @Query("SELECT q FROM PeriodicQuiz pq JOIN pq.questions q WHERE pq.id = :quizId")
    Page<Question> findQuestionsByQuizId(@Param("quizId") Long quizId, Pageable pageable);

    @Query("SELECT COUNT(q) FROM PeriodicQuiz pq JOIN pq.questions q WHERE pq.id = :quizId")
    Long countQuestionsByQuizId(@Param("quizId") Long quizId);

    Optional<PeriodicQuiz> findPeriodicQuizByQuizId(String quizId);
}

