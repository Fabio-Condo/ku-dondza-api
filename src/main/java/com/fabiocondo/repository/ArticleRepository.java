package com.fabiocondo.repository;

import com.fabiocondo.domain.Article;
import com.fabiocondo.repository.query.ArticleRepositoryQuery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ArticleRepository extends JpaRepository<Article, Long>, ArticleRepositoryQuery {
    Optional<Article> findArticleByArticleId(String postId);
}
