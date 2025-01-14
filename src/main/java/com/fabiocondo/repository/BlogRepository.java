package com.fabiocondo.repository;

import com.fabiocondo.domain.Blog;
import com.fabiocondo.repository.query.BlogRepositoryQuery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BlogRepository extends JpaRepository<Blog, Long>, BlogRepositoryQuery {
    Optional<Blog> findBlogByBlogId(String blogId);
}
