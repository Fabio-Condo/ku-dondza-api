package com.fabiocondo.service;

import com.fabiocondo.domain.Institution;
import com.fabiocondo.enumeration.AdministrationType;
import com.fabiocondo.exception.domain.ExamNotFoundException;
import com.fabiocondo.exception.domain.InstituicaoNotFoundException;
import com.fabiocondo.repository.filter.InstitutionFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface InstitutionService {
    Institution findById(Long id) throws InstituicaoNotFoundException;

    Institution findInstitutionByInstitutionId(String institutionId) throws InstituicaoNotFoundException;

    Page<Institution> findAll(Pageable pageable);

    Page<Institution> filter(InstitutionFilter institutionFilter, Pageable pageable);

    Institution save(String name, String acronym, String type, AdministrationType administrationType, String address, String description, String website, MultipartFile file) throws InstituicaoNotFoundException;

    Institution update(Long id, String name, String acronym, String type, AdministrationType administrationType, String address, String description, String website, MultipartFile file) throws ExamNotFoundException, InstituicaoNotFoundException;

    void delete(Long id) throws InstituicaoNotFoundException;

    long getTotal();
}
