package com.fabiocondo.service;

import com.fabiocondo.domain.Article;
import com.fabiocondo.enumeration.CategoryType;
import com.fabiocondo.exception.domain.ArticleNotFoundException;
import com.fabiocondo.exception.domain.SubjectNotFoundException;
import com.fabiocondo.repository.filter.ArticleFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface ArticleService {
    Article findById(Long id) throws ArticleNotFoundException;

    Article findArticleByArticleId(String articleId) throws ArticleNotFoundException;

    Page<Article> filter(ArticleFilter articleFilter, Pageable pageable);

    Article save(String title, String content, CategoryType category, int readingTimeMinutes, MultipartFile file) throws SubjectNotFoundException;

    Article update(Long id, String title, String content, CategoryType category, int readingTimeMinutes, MultipartFile file) throws ArticleNotFoundException, SubjectNotFoundException;

    void delete(Long id) throws ArticleNotFoundException;

    long getTotal();
}
