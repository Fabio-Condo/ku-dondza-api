package com.fabiocondo.repository;

import com.fabiocondo.domain.Question;
import com.fabiocondo.domain.Quiz;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
    @Query("SELECT qts FROM Quiz qz JOIN qz.questions qts WHERE qz.id = :quizId")
    Page<Question> findQuestionsByQuizId(@Param("quizId") Long quizId, Pageable pageable);

    @Query("SELECT COUNT(qts) FROM Quiz qz JOIN qz.questions qts WHERE qz.id = :quizId")
    Long countQuestionsByQuizId(@Param("quizId") Long quizId);
}
