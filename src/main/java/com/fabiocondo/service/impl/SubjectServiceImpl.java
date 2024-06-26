package com.fabiocondo.service.impl;

import com.fabiocondo.domain.Subject;
import com.fabiocondo.exception.domain.SubjectNotFoundException;
import com.fabiocondo.repository.SubjectRepository;
import com.fabiocondo.service.SubjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class SubjectServiceImpl implements SubjectService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    SubjectRepository subjectRepository;

    public SubjectServiceImpl(SubjectRepository subjectRepository) {
        this.subjectRepository = subjectRepository;
    }

    @Override
    public Subject findById(Long id) throws SubjectNotFoundException {
        logger.info("Getting subject by id: " + id);
        return subjectRepository.findById(id)
                .orElseThrow(() -> new SubjectNotFoundException("No subject found by id: " + id));
    }

    @Override
    public List<Subject> findAll() {
        return subjectRepository.findAll();
    }

    @Override
    public Subject save(Subject subject) {
        return subjectRepository.save(subject);
    }
}
