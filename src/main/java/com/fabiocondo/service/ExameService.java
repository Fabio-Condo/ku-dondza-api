package com.fabiocondo.service;

import com.fabiocondo.domain.Exame;
import com.fabiocondo.exception.domain.ExameNotFoundException;
import com.fabiocondo.exception.domain.InstituicaoNotFoundException;
import com.fabiocondo.exception.domain.SubjectNotFoundException;
import com.fabiocondo.repository.filter.ExameFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;

public interface ExameService {
    Exame findById(Long id) throws ExameNotFoundException;

    Page<Exame> filter(ExameFilter exameFilter, Pageable pageable);

    Page<Exame> findAll(Pageable pageable);

    List<Exame> findAll();

    Exame save(String description, Date date, Long subjectId, Long institutionId, MultipartFile file) throws InstituicaoNotFoundException, SubjectNotFoundException;

    Exame update(Long id, String description, Date date, Long subjectId, Long institutionId, MultipartFile file) throws ExameNotFoundException, InstituicaoNotFoundException, SubjectNotFoundException;

    void delete(Long id) throws ExameNotFoundException;

    byte[] downloadFile(Long id, @PathVariable String fileName) throws ExameNotFoundException;

    long getTotal();
}
