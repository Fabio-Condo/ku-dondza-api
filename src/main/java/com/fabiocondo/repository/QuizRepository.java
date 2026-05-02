package com.fabiocondo.repository;

import com.fabiocondo.domain.Answer;
import com.fabiocondo.domain.Question;
import com.fabiocondo.domain.Quiz;
import com.fabiocondo.domain.User;
import com.fabiocondo.repository.query.QuizRepositoryQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface QuizRepository extends JpaRepository<Quiz, Long>, QuizRepositoryQuery {
    //@Query("SELECT q FROM Quiz q WHERE q.title LIKE %:searchParam%")
    //public Page<Quiz> findAll(@Param("searchParam") String searchParam, Pageable pageable);

    @Query("SELECT usa FROM Quiz qz JOIN qz.answers usa WHERE qz.id = :quizId")
    Set<Answer> findAnswersByQuizId(@Param("quizId") Long quizId);

    @Query("SELECT qts FROM Quiz qz JOIN qz.questions qts WHERE qz.id = :quizId")
    Set<Question> findQuestionsByQuizId_v2(@Param("quizId") Long quizId);

    @Query("SELECT COUNT(qts) FROM Quiz qz JOIN qz.questions qts WHERE qz.id = :quizId")
    Long countQuestionsByQuizId(@Param("quizId") Long quizId);

    Optional<Quiz> findQuizByQuizId(String quizId);

    Long countByUserId(@Param("userId") Long userId);

    long countByAnswers_Question(Question question);

    public long countByAnswers_IsCorrectTrueAndAnswers_Question(Question question);

    public long countByAnswers_IsNullAndAnswers_Question(Question question);

    long countByQuestions(Question question);

    public Page<Quiz> findAllByQuestions(Question question, Pageable pageable);

    long countByUser(User user);

    @Query(
            value = "SELECT qa.quiz_id, " +
                    "COUNT(a.id) AS total_answers, " +
                    "SUM(CASE WHEN a.is_correct = true THEN 1 ELSE 0 END) AS correct_answers " +
                    "FROM quiz_answers qa " +
                    "INNER JOIN answer a ON qa.answer_id = a.id " +
                    "INNER JOIN quiz q ON q.id = qa.quiz_id " +
                    "WHERE q.user_id = :userId " +
                    "AND q.subject_id = :subjectId " +
                    "GROUP BY qa.quiz_id",
            nativeQuery = true
    )
    List<Object[]> findAccuracyStats(Long userId, Long subjectId);

}
