package com.fabiocondo.repository;

import com.fabiocondo.domain.Subject;
import com.fabiocondo.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SubjectRepository extends JpaRepository<Subject, Long> {

    @Query("SELECT s FROM Subject s WHERE s.name LIKE %:name%")
    public Page<Subject> findByName(@Param("name") String name, Pageable pageable);

    Optional<Subject> findSubjectBySubjectId(String subjectId);

    @Query("SELECT u FROM Subject s JOIN s.students u WHERE s.id = :subjectId")
    Page<User> findStudentsBySubjectId(@Param("subjectId") Long subjectId, Pageable pageable);

    @Query("SELECT COUNT(u) FROM Subject s JOIN s.students u WHERE s.id = :subjectId")
    Long countStudentsBySubjectId(@Param("subjectId") Long subjectId);

}
