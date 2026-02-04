package com.fabiocondo.service;

import com.fabiocondo.domain.Exam;
import com.fabiocondo.enumeration.ExamType;
import com.fabiocondo.enumeration.Institution;
import com.fabiocondo.exception.domain.ExamNotFoundException;
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

    Exam save(ExamType status, Institution institution, boolean premium, Date date, Long subjectId, String number, MultipartFile file) throws SubjectNotFoundException;

    Exam update(Long id, ExamType status, Institution institution, boolean premium, Date date, Long subjectId, String number, MultipartFile file) throws ExamNotFoundException, SubjectNotFoundException;

    void delete(Long id) throws ExamNotFoundException;

    byte[] downloadFile(Long id, @PathVariable String fileName) throws ExamNotFoundException;

    long getTotal();
}