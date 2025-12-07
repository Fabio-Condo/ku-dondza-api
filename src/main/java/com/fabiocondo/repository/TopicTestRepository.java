package com.fabiocondo.repository;

import com.fabiocondo.domain.TopicTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TopicTestRepository extends JpaRepository<TopicTest, Long> {
    @Query("SELECT t FROM TopicTest t WHERE t.topic.subject.id = :subjectId ORDER BY t.orderIndex ASC")
    List<TopicTest> findBySubjectId(@Param("subjectId") Long subjectId);

    //List<TopicTest> findByTopic_Subject_Id(Long subjectId);

}
