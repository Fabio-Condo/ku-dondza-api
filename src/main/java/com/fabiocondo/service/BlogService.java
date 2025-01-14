package com.fabiocondo.service;

import com.fabiocondo.domain.Blog;
import com.fabiocondo.exception.domain.BlogNotFoundException;
import com.fabiocondo.exception.domain.SubjectNotFoundException;
import com.fabiocondo.repository.filter.BlogFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface BlogService {
    Blog findById(Long id) throws BlogNotFoundException;

    Blog findBlogByBlogId(String blogId) throws BlogNotFoundException;

    Page<Blog> filter(BlogFilter blogFilter, Pageable pageable);

    Blog save(String title, String content, Long subjectId, MultipartFile file) throws SubjectNotFoundException;

    Blog update(Long id, String title, String content, Long subjectId, MultipartFile file) throws BlogNotFoundException, SubjectNotFoundException;

    void delete(Long id) throws BlogNotFoundException;

    long getTotal();
}
