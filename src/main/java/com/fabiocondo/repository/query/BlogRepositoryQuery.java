package com.fabiocondo.repository.query;

import com.fabiocondo.domain.Blog;
import com.fabiocondo.repository.filter.BlogFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BlogRepositoryQuery {
    public Page<Blog> filter(BlogFilter blogFilter, Pageable pageable);
}
