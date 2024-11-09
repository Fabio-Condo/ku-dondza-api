package com.fabiocondo.repository;

import com.fabiocondo.domain.OnlineCourse;
import com.fabiocondo.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OnlineCourseRepository extends JpaRepository<OnlineCourse, Long> {
    @Query("SELECT oc FROM OnlineCourse oc WHERE oc.name LIKE %:searchParam%")
    public Page<OnlineCourse> findAll(@Param("searchParam") String searchParam, Pageable pageable);

    @Query("SELECT u FROM OnlineCourse oc JOIN oc.students u WHERE oc.id = :courseId")
    Page<User> findStudentsByCourseId(@Param("courseId") Long courseId, Pageable pageable);

    @Query("SELECT COUNT(u) FROM OnlineCourse oc JOIN oc.students u WHERE oc.id = :courseId")
    Long countOnlineCourseStudentsByCourseId(@Param("courseId") Long courseId);
}
