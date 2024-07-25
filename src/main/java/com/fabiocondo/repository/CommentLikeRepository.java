package com.fabiocondo.repository;

import com.fabiocondo.domain.Comment;
import com.fabiocondo.domain.CommentLike;
import com.fabiocondo.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {
    Optional<CommentLike> findByCommentAndUser(Comment comment, User user);
    void deleteByCommentAndUser(Comment comment, User user);
    Long countByCommentId(Long commentId);
    boolean existsByCommentIdAndUserId(Long commentId, Long userId);
    Page<CommentLike> findByCommentId(Long commentId, Pageable pageable);
}
