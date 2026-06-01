package com.fabiocondo.repository;

import com.fabiocondo.domain.Question;
import com.fabiocondo.repository.query.QuestionRepositoryQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface QuestionRepository extends JpaRepository<Question, Long>, QuestionRepositoryQuery {
    @Query("SELECT q FROM Question q WHERE q.text LIKE %:searchParam%")
    public Page<Question> findAll(@Param("searchParam") String searchParam, Pageable pageable);

    @Query("SELECT q FROM Question q WHERE q.topic.id IN :topicIds")
    Set<Question> findByTopicIdIn(@Param("topicIds") Set<Long> topicIds);

    //@Query("SELECT q FROM Question q WHERE q.topic.id IN :topicIds AND q.difficultyLevel = :difficultyLevel")
    //Set<Question> findByTopicIdInAndDifficultyLevel(
    //        @Param("topicIds") Set<Long> topicIds,
    //        @Param("difficultyLevel") DifficultyLevel difficultyLevel);

    Optional<Question> findQuestionByQuestionId(String questionId);

    Set<Question> findByTopicId(Long topicId);

    long countByTopicId(Long topicId);

    List<Question> findByTopicSubjectId(Long subjectId);

    Page<Question> findByHighlightedTrue(Pageable pageable);

}
