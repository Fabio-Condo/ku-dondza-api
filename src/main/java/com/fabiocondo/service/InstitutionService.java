package com.fabiocondo.service;

import com.fabiocondo.domain.Institution;
import com.fabiocondo.exception.domain.InstituicaoNotFoundException;
import com.fabiocondo.repository.InstitutionRepository;
import com.fabiocondo.repository.filter.InstitutionFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class InstitutionService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    InstitutionRepository institutionRepository;

    public InstitutionService(InstitutionRepository institutionRepository) {
        this.institutionRepository = institutionRepository;
    }

    public Institution findById(Long id) throws InstituicaoNotFoundException {
        logger.info("Getting institution by id: " + id);
        return institutionRepository.findById(id)
                .orElseThrow(() -> new InstituicaoNotFoundException("No institution found by id: " + id));
    }

    public Page<Institution> findAll(Pageable pageable) {
        return institutionRepository.findAll(pageable);
    }

    public Page<Institution> filter(InstitutionFilter institutionFilter, Pageable pageable){
        return institutionRepository.filter(institutionFilter, pageable);
    }

    public Institution save(Institution institution)  {
        logger.info("Saving institution: " + institution.getName());
        return institutionRepository.save(institution);
    }

    public Institution update(Institution institution, Long id) throws InstituicaoNotFoundException {
        Institution existInstitution = findById(id);
        BeanUtils.copyProperties(institution, existInstitution, "id");
        logger.info("Updating institution: " + institution.getName());
        return institutionRepository.save(existInstitution);
    }

    public void delete(Long id) throws InstituicaoNotFoundException {
        Institution institution = findById(id);
        logger.info("Deleting institution: " + institution.getName());
        institutionRepository.deleteById(id);
    }

    public long getTotal(){
        logger.info("Total institution: " + institutionRepository.count());
        return institutionRepository.count();
    }
}
