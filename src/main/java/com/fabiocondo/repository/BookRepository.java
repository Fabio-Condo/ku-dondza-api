package com.fabiocondo.repository;

import com.fabiocondo.domain.Book;
import com.fabiocondo.repository.query.BookRepositoryQuery;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Long>, BookRepositoryQuery {
}
