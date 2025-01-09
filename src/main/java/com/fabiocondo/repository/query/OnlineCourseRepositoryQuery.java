package com.fabiocondo.repository.query;

import com.fabiocondo.domain.OnlineCourse;
import com.fabiocondo.repository.filter.OnlineCourseFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OnlineCourseRepositoryQuery {
    public Page<OnlineCourse> filter(OnlineCourseFilter onlineCourseFilter, Pageable pageable);
}
