package com.fabiocondo.repository;

import com.fabiocondo.domain.TopicContent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TopicContentRepository extends JpaRepository<TopicContent, Long> {
    public Page<TopicContent> findByTopicId(Long topicId, Pageable pageable);
}
