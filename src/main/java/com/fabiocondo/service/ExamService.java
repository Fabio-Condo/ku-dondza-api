package com.fabiocondo.service;

import com.fabiocondo.domain.Exam;
import com.fabiocondo.enumeration.ExamStatus;
import com.fabiocondo.exception.domain.ExamNotFoundException;
import com.fabiocondo.exception.domain.InstituicaoNotFoundException;
import com.fabiocondo.exception.domain.SubjectNotFoundException;
import com.fabiocondo.repository.filter.ExamFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;

public interface ExamService {
    Exam findById(Long id) throws ExamNotFoundException;

    Page<Exam> filter(ExamFilter examFilter, Pageable pageable);

    Page<Exam> findAll(Pageable pageable);

    List<Exam> findAll();

    Exam save(String description, ExamStatus status, Date date, Long subjectId, Long institutionId, MultipartFile file) throws InstituicaoNotFoundException, SubjectNotFoundException;

    Exam update(Long id, String description, ExamStatus status, Date date, Long subjectId, Long institutionId, MultipartFile file) throws ExamNotFoundException, InstituicaoNotFoundException, SubjectNotFoundException;

    void delete(Long id) throws ExamNotFoundException;

    byte[] downloadFile(Long id, @PathVariable String fileName) throws ExamNotFoundException;

    long getTotal();
}
