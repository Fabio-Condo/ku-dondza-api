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

    //@Query("SELECT q FROM Test tt JOIN tt.submittedQuizzes q " + "WHERE tt.id IN :topicTestIds AND q.user.id = :userId")
    //List<Quiz> findUserQuizzesByTopicTests(@Param("topicTestIds") List<Long> topicTestIds, @Param("userId") Long userId);
}
