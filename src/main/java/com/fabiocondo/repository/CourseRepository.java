package com.fabiocondo.repository;

import com.fabiocondo.domain.Course;
import com.fabiocondo.domain.User;
import com.fabiocondo.repository.query.CourseRepositoryQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long>, CourseRepositoryQuery {
    @Query("SELECT oc FROM Course oc WHERE oc.name LIKE %:searchParam%")
    public Page<Course> findAll(@Param("searchParam") String searchParam, Pageable pageable);

    @Query("SELECT u FROM Course oc JOIN oc.students u WHERE oc.id = :courseId")
    Page<User> findStudentsByCourseId(@Param("courseId") Long courseId, Pageable pageable);

    @Query("SELECT COUNT(u) FROM Course c JOIN c.students u WHERE c.id = :courseId")
    Long countStudentsByCourseId(@Param("courseId") Long courseId);

    Optional<Course> findOnlineCourseByOnlineCourseId(String onlineCourseId);
}
