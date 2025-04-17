package com.fabiocondo.repository;

import com.fabiocondo.domain.UserCourse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserCourseRepository extends JpaRepository<UserCourse, Long> {
    Optional<UserCourse> findByUserIdAndCourseId(Long userId, Long courseId);
    Page<UserCourse> findByUserId(Long userId, Pageable pageable);
    Page<UserCourse> findByCourseId(Long courseId, Pageable pageable);
    boolean existsByUserIdAndCourseId(Long userId, Long courseId);

}
