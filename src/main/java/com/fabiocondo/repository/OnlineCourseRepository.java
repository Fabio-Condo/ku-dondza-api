package com.fabiocondo.repository;

import com.fabiocondo.domain.OnlineCourse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OnlineCourseRepository extends JpaRepository<OnlineCourse, Long> {
    @Query("SELECT COUNT(u) FROM OnlineCourse oc JOIN oc.students u WHERE oc.id = :courseId")
    Long countOnlineCourseStudentsByCourseId(@Param("courseId") Long courseId);
}
