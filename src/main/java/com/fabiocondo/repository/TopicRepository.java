package com.fabiocondo.repository;

import com.fabiocondo.domain.Topic;
import com.fabiocondo.repository.query.TopicRepositoryQuery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TopicRepository extends JpaRepository<Topic, Long>, TopicRepositoryQuery {
    List<Topic> findBySubjectIdOrderByNameAsc(Long subjectId);

    List<Topic> findBySubjectIdAndIsReadyForQuizTrueOrderByNameAsc(Long subjectId);

    Optional<Topic> findTopicByTopicId(String topicId);

    long countBySubjectId(Long subjectId);
}
