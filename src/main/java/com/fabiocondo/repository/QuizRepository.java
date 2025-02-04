package com.fabiocondo.repository;

import com.fabiocondo.domain.Answer;
import com.fabiocondo.domain.Question;
import com.fabiocondo.domain.Quiz;
import com.fabiocondo.repository.query.QuizRepositoryQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface QuizRepository extends JpaRepository<Quiz, Long>, QuizRepositoryQuery {
    @Query("SELECT q FROM Quiz q WHERE q.description LIKE %:searchParam%")
    public Page<Quiz> findAll(@Param("searchParam") String searchParam, Pageable pageable);

    @Query("SELECT usa FROM Quiz qz JOIN qz.userSubmittedAnswers usa WHERE qz.id = :quizId")
    List<Answer> findUserSubmittedAnswersByQuizId(@Param("quizId") Long quizId);

    @Query("SELECT qts FROM Quiz qz JOIN qz.questions qts WHERE qz.id = :quizId")
    List<Question> findQuestionsByQuizId(@Param("quizId") Long quizId);

    @Query("SELECT COUNT(qts) FROM Quiz qz JOIN qz.questions qts WHERE qz.id = :quizId")
    Long countQuestionsByQuizId(@Param("quizId") Long quizId);

    Optional<Quiz> findQuizByQuizId(String quizId);

    Long countByUserId(@Param("userId") Long userId);

}
