package com.fabiocondo.repository;

import com.fabiocondo.domain.Module;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TemaRepository extends JpaRepository<Module, Long> {

    @Query("SELECT t FROM Module t WHERE t.name LIKE %:searchParam%")
    public Page<Module> findAll(@Param("searchParam") String searchParam, Pageable pageable);

    public Page<Module> findByOnlineCourseIdOrderByPositionAsc(Long courseId, Pageable pageable);

    public List<Module> findByOnlineCourseId(Long courseId);

}
