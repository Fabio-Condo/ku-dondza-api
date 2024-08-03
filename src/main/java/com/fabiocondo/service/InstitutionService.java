package com.fabiocondo.service;

import com.fabiocondo.domain.Institution;
import com.fabiocondo.enumeration.AdministrationType;
import com.fabiocondo.enumeration.Country;
import com.fabiocondo.exception.domain.ExameNotFoundException;
import com.fabiocondo.exception.domain.InstituicaoNotFoundException;
import com.fabiocondo.repository.filter.InstitutionFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface InstitutionService {
    Institution findById(Long id) throws InstituicaoNotFoundException;

    Page<Institution> findAll(Pageable pageable);

    Page<Institution> filter(InstitutionFilter institutionFilter, Pageable pageable);

    Institution save(String name, String acronym, String type, AdministrationType administrationType, Country country, String address, String description, String website, MultipartFile file) throws InstituicaoNotFoundException;

    Institution update(Long id, String name, String acronym, String type, AdministrationType administrationType, Country country, String address, String description, String website, MultipartFile file) throws ExameNotFoundException, InstituicaoNotFoundException;

    void delete(Long id) throws InstituicaoNotFoundException;

    long getTotal();
}
