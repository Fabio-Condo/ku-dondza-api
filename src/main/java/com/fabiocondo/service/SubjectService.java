package com.fabiocondo.service;

import com.fabiocondo.domain.Subject;
import com.fabiocondo.exception.domain.SubjectNotFoundException;
import com.fabiocondo.repository.SubjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class SubjectService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    SubjectRepository subjectRepository;

    public SubjectService(SubjectRepository subjectRepository) {
        this.subjectRepository = subjectRepository;
    }

    public Subject findById(Long id) throws SubjectNotFoundException {
        logger.info("Getting subject by id: " + id);
        return subjectRepository.findById(id)
                .orElseThrow(() -> new SubjectNotFoundException("No subject found by id: " + id));
    }

    public List<Subject> findAll() {
        return subjectRepository.findAll();
    }

    public Subject save(Subject subject) {
        return subjectRepository.save(subject);
    }
}
