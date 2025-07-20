package com.fabiocondo.repository;

import com.fabiocondo.domain.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    Page<Comment> findByQuestionIdOrderByCreatedAtDesc(Long exerciseId, Pageable pageable);

    @Query("SELECT COUNT(c) FROM Comment c WHERE c.question.id = :questionId")
    Long countCommentsByQuestionId(@Param("questionId") Long questionId);
}

