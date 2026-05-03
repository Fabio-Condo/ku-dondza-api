package com.fabiocondo.repository;

import com.fabiocondo.domain.TopicContent;
import com.fabiocondo.enumeration.ContentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TopicContentRepository extends JpaRepository<TopicContent, Long> {
    public Page<TopicContent> findByTopicId(Long topicId, Pageable pageable);

    // Buscando os conteúdos da disciplina através do tópico
    List<TopicContent> findByTopic_Subject_Id(Long subjectId);

    @Query("SELECT COUNT(tc) " +
            "FROM TopicContent tc " +
            "WHERE tc.contentType = :contentType " +
            "AND tc.topic.subject.id = :subjectId")
    Long countBySubjectAndContentType(@Param("subjectId") Long subjectId,
                                      @Param("contentType") ContentType contentType);

}
