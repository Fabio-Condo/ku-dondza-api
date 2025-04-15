package com.fabiocondo.repository;

import com.fabiocondo.domain.OnlineCourseContent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OnlineCourseContentRepository extends JpaRepository<OnlineCourseContent, Long> {

    public Page<OnlineCourseContent> findByModuleId(Long moduleId, Pageable pageable);

    // Buscando os conteúdos do curso através do módulo
    List<OnlineCourseContent> findByModule_OnlineCourse_Id(Long courseId);}
