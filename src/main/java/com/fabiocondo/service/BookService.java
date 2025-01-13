package com.fabiocondo.service;

import com.fabiocondo.domain.Book;
import com.fabiocondo.exception.domain.BookNotFoundException;
import com.fabiocondo.exception.domain.SubjectNotFoundException;
import com.fabiocondo.repository.filter.BookFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;

public interface BookService {
    Book findById(Long id) throws BookNotFoundException;

    Page<Book> filter(BookFilter bookFilter, Pageable pageable);

    Page<Book> findAll(Pageable pageable);

    List<Book> findAll();

    Book save(String name, String description, Long subjectId, MultipartFile file) throws SubjectNotFoundException;

    Book update(Long id, String name, String description, Long subjectId, MultipartFile file) throws BookNotFoundException, SubjectNotFoundException;

    void delete(Long id) throws BookNotFoundException;

    byte[] downloadFile(Long id, @PathVariable String fileName) throws BookNotFoundException;

    long getTotal();
}
