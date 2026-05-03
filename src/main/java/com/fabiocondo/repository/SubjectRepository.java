package com.fabiocondo.repository;

import com.fabiocondo.domain.Subject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SubjectRepository extends JpaRepository<Subject, Long> {

    @Query("SELECT s FROM Subject s " +
            "WHERE s.name LIKE %:name% " +
            "AND s.courseEnabled = true")
    Page<Subject> findByName(@Param("name") String name, Pageable pageable);

    @Query("SELECT s FROM Subject s " +
            "WHERE s.name LIKE %:name% " +
            "AND s.courseEnabled = :enabled")
    Page<Subject> findByNameAndCourseEnabled(@Param("name") String name, @Param("enabled") boolean enabled, Pageable pageable);

    Optional<Subject> findSubjectBySubjectId(String subjectId);
}
