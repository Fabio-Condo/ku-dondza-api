package com.fabiocondo.repository;

import com.fabiocondo.domain.Quiz;
import com.fabiocondo.domain.Test;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TopicTestRepository extends JpaRepository<Test, Long> {
    @Query("SELECT t FROM Test t WHERE t.topic.subject.id = :subjectId ORDER BY t.orderIndex ASC")
    List<Test> findBySubjectId(@Param("subjectId") Long subjectId);

    @Query("SELECT q FROM Test tt " + "JOIN tt.submittedQuizzes q " + "WHERE tt.id = :topicTestId " + "AND q.user.id = :userId")
    Optional<Quiz> findUserQuizByTopicTest(@Param("topicTestId") Long topicTestId, @Param("userId") Long userId);

    @Query("SELECT CASE WHEN COUNT(q) > 0 THEN true ELSE false END " +
            "FROM Test t " +
            "JOIN t.submittedQuizzes q " +
            "WHERE t.id = :topicTestId " +
            "AND q.user.id = :userId")
    boolean existsQuizInTest(@Param("topicTestId") Long topicTestId,
                             @Param("userId") Long userId);

    List<Test> findByTopicSubjectId(Long subjectId);

    @Query("SELECT q " +
            "FROM Test t " +
            "JOIN t.submittedQuizzes q " +
            "WHERE t.id = :testId " +
            "AND q.user.id = :userId")
    Optional<Quiz> findUserQuizByTest(@Param("testId") Long testId,
                                      @Param("userId") Long userId);

    @Query("SELECT COUNT(DISTINCT t.id) " +
            "FROM Test t JOIN t.submittedQuizzes q " +
            "WHERE q.user.id = :userId " +
            "AND t.topic.subject.id = :subjectId")
    Long countCompletedTestsByUserAndSubject(@Param("userId") Long userId,
                                             @Param("subjectId") Long subjectId);

    @Query("SELECT DISTINCT t.id " +
            "FROM Test t " +
            "JOIN t.submittedQuizzes q " +
            "WHERE q.user.id = :userId " +
            "AND t.topic.subject.id = :subjectId")
    List<Long> findCompletedTestIds(@Param("userId") Long userId,
                                    @Param("subjectId") Long subjectId);

    @Query("SELECT t.id, COUNT(q) " +
            "FROM Test t JOIN t.questions q " +
            "WHERE t.topic.subject.id = :subjectId " +
            "GROUP BY t.id")
    List<Object[]> countQuestionsPerTest(@Param("subjectId") Long subjectId);

    //@Query("SELECT q FROM Test tt JOIN tt.submittedQuizzes q " + "WHERE tt.id IN :topicTestIds AND q.user.id = :userId")
    //List<Quiz> findUserQuizzesByTopicTests(@Param("topicTestIds") List<Long> topicTestIds, @Param("userId") Long userId);
}
