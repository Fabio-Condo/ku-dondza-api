package com.fabiocondo.repository;

import com.fabiocondo.domain.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {
    Optional<CommentLike> findByCommentAndUser(Comment comment, User user);

    boolean existsByCommentIdAndUserId(Long commentId, Long userId);

    @Query("SELECT COUNT(l) FROM CommentLike l WHERE l.comment.id = :commentId")
    Long countLikesByCommentId(@Param("commentId") Long postId);

    Page<CommentLike> findByCommentId(Long commentId, Pageable pageable);
}