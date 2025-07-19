package com.fabiocondo.repository;

import com.fabiocondo.domain.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    Page<Comment> findByQuestionIdOrderByCreatedAtDesc(Long exerciseId, Pageable pageable);
}

