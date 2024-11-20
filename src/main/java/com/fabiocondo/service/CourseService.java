package com.fabiocondo.service;

import com.fabiocondo.domain.Course;
import com.fabiocondo.exception.domain.CourseNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface CourseService {
    Course findById(Long id) throws CourseNotFoundException;

    List<Course> getByInstitutionId(Long institutionId);

    Page<Course> findAll(Pageable pageable);

    Page<Course> findByName(String name, Pageable pageable);

    Page<Course> findByInstitutionId(@RequestParam Long institutionId, Pageable pageable);

    Course save(Course course);

    Course update(Course course, Long id) throws CourseNotFoundException;

    void delete(Long id) throws CourseNotFoundException;

    long getTotal();
}
