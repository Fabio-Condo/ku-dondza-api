package com.fabiocondo.service;

import com.fabiocondo.domain.Course;
import com.fabiocondo.domain.Subject;
import com.fabiocondo.exception.domain.SubjectNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SubjectService {
    Subject findById(Long id) throws SubjectNotFoundException;

    List<Subject> findAll();

    Page<Subject> findAll(Pageable pageable);

    Subject save(Subject subject);

    Subject update(Subject subject, Long id) throws SubjectNotFoundException;
}
