package com.fabiocondo.repository;

import com.fabiocondo.domain.Course;
import com.fabiocondo.domain.OnlineCourseContent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OnlineCourseContentRepository extends JpaRepository<OnlineCourseContent, Long> {

    public Page<OnlineCourseContent> findByOnlineCourseId(Long onlineCourseId, Pageable pageable);

}
