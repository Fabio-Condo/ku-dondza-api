package com.fabiocondo.repository.query;

import com.fabiocondo.domain.Course;
import com.fabiocondo.repository.filter.CourseFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CourseRepositoryQuery {
    public Page<Course> filter(CourseFilter courseFilter, Pageable pageable);
}
