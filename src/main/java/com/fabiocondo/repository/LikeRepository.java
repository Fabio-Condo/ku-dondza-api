package com.fabiocondo.repository;

import com.fabiocondo.domain.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Long> {
    Optional<Like> findByArticleAndUser(Article article, User user);

    boolean existsByArticleIdAndUserId(Long articleId, Long userId);

    @Query("SELECT COUNT(l) FROM Like l WHERE l.article.id = :articleId")
    Long countLikesByArticleId(@Param("articleId") Long articleId);

    Page<Like> findByArticleId(Long articleId, Pageable pageable);
}