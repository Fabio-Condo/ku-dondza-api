package com.fabiocondo.repository.query;

import com.fabiocondo.domain.Book;
import com.fabiocondo.repository.filter.BookFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BookRepositoryQuery {
    public Page<Book> filter(BookFilter bookFilter, Pageable pageable);
}
