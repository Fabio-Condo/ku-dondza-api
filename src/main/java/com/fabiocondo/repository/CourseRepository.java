package com.fabiocondo.repository;

import com.fabiocondo.domain.Course;
import com.fabiocondo.domain.Teacher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CourseRepository extends JpaRepository<Course, Long> {
    @Query("SELECT c FROM Course c WHERE c.name LIKE %:name% OR c.institution.name LIKE %:name% OR c.institution.type LIKE %:name% ORDER BY name ASC")
    public Page<Course> findByName(@Param("name") String name, Pageable pageable);
}
