package com.fabiocondo.repository;

import com.fabiocondo.domain.Tema;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TemaRepository extends JpaRepository<Tema, Long> {

    @Query("SELECT t FROM Tema t WHERE t.name LIKE %:searchParam%")
    public Page<Tema> findAll(@Param("searchParam") String searchParam, Pageable pageable);

    public Page<Tema> findByOnlineCourseId(Long courseId, Pageable pageable);

    public List<Tema> findByOnlineCourseId(Long courseId);

}
