package com.fabiocondo.service;

import com.fabiocondo.domain.Subject;
import com.fabiocondo.enumeration.Category;
import com.fabiocondo.exception.domain.SubjectNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface SubjectService {
    Subject findById(Long id) throws SubjectNotFoundException;

    List<Subject> findAll();

    Page<Subject> findAll(Pageable pageable);

    Page<Subject> findByName(String name, Pageable pageable);

    Subject save(String name, String description, Category category, boolean quizEnabled, boolean courseEnabled, boolean progressEnabled, boolean examEnabled, MultipartFile file);

    Subject update(Long id, String name, String description, Category category, boolean quizEnabled, boolean courseEnabled, boolean progressEnabled, boolean examEnabled, MultipartFile file) throws SubjectNotFoundException;
}
