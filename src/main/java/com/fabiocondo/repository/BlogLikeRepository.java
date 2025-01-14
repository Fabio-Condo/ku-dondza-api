package com.fabiocondo.repository;

import com.fabiocondo.domain.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BlogLikeRepository extends JpaRepository<BlogLike, Long> {
    Optional<BlogLike> findByBlogAndUser(Blog blog, User user);

    boolean existsByBlogIdAndUserId(Long blogId, Long userId);

    @Query("SELECT COUNT(l) FROM BlogLike l WHERE l.blog.id = :blogId")
    Long countLikesByBlogId(@Param("blogId") Long blogId);

    Page<BlogLike> findByBlogId(Long blogId, Pageable pageable);
}
