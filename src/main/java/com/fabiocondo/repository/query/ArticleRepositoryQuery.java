package com.fabiocondo.repository.query;

import com.fabiocondo.domain.Article;
import com.fabiocondo.repository.filter.ArticleFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ArticleRepositoryQuery {
    public Page<Article> filter(ArticleFilter articleFilter, Pageable pageable);
}
