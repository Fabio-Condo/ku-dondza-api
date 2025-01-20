package com.fabiocondo.repository;

import com.fabiocondo.domain.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OnlineCourseLikeRepository extends JpaRepository<OnlineCourseLike, Long> {
    Optional<OnlineCourseLike> findByOnlineCourseAndUser(OnlineCourse onlineCourse, User user);

    boolean existsByOnlineCourseIdAndUserId(Long onlineCourseId, Long userId);

    @Query("SELECT COUNT(l) FROM OnlineCourseLike l WHERE l.onlineCourse.id = :onlineCourseId")
    Long countLikesByOnlineCourseId(@Param("onlineCourseId") Long onlineCourseId);

    Page<OnlineCourseLike> findByOnlineCourseId(Long onlineCourseId, Pageable pageable);
}
