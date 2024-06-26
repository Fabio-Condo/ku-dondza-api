package com.fabiocondo.service;

import com.fabiocondo.domain.Subject;
import com.fabiocondo.exception.domain.SubjectNotFoundException;

import java.util.List;

public interface SubjectService {
    Subject findById(Long id) throws SubjectNotFoundException;

    List<Subject> findAll();

    Subject save(Subject subject);
}
