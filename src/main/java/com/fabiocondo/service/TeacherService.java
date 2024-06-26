package com.fabiocondo.service;

import com.fabiocondo.domain.Teacher;
import com.fabiocondo.exception.domain.CombinationExistException;
import com.fabiocondo.exception.domain.SubjectNotFoundException;
import com.fabiocondo.exception.domain.TeacherNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface TeacherService {
    Teacher findById(Long id) throws TeacherNotFoundException;

    Page<Teacher> findAll(Pageable pageable);

    Page<Teacher> findByName(String name, Pageable pageable);

    Teacher save(String name, String email, String contactNumber, MultipartFile file);

    Teacher update(Long id, String name, String email, String contactNumber, MultipartFile file) throws TeacherNotFoundException;

    void delete(Long id) throws TeacherNotFoundException;

    Teacher addSubjectToTeacherSubjectsList(Long teacherId, Long subjectId) throws TeacherNotFoundException, SubjectNotFoundException, CombinationExistException;

    Teacher removeSubjectFromTeacherSubjectsList(Long teacherId, Long subjectId) throws TeacherNotFoundException, SubjectNotFoundException;

    long getTotal();
}
