package com.fabiocondo.repository;

import com.fabiocondo.domain.Content;
import com.fabiocondo.domain.TopicContent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TopicContentRepository extends JpaRepository<TopicContent, Long> {
    public Page<TopicContent> findByTopicId(Long topicId, Pageable pageable);

    // Buscando os conteúdos da disciplina através do tópico
    List<TopicContent> findByTopic_Subject_Id(Long subjectId);
}
