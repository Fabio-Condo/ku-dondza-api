package com.fabiocondo.repository;

import com.fabiocondo.domain.Content;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContentRepository extends JpaRepository<Content, Long> {

    public Page<Content> findByModuleId(Long moduleId, Pageable pageable);

    // Buscando os conteúdos do curso através do módulo
    List<Content> findByModule_Course_Id(Long courseId);}
