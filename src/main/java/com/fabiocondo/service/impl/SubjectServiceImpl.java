package com.fabiocondo.service.impl;

import com.fabiocondo.domain.Subject;
import com.fabiocondo.exception.domain.SubjectNotFoundException;
import com.fabiocondo.repository.SubjectRepository;
import com.fabiocondo.service.SubjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    public Page<Subject> findAll(Pageable pageable) {
        return subjectRepository.findAll(pageable);
    }

    @Override
    public Subject save(Subject subject) {
        return subjectRepository.save(subject);
    }

    @Override
    public Subject update(Subject subject, Long id) throws SubjectNotFoundException {
        Subject existSubject = findById(id);
        BeanUtils.copyProperties(subject, existSubject, "id", "topics");
        logger.info("Updating subject: " + subject.getName());
        return subjectRepository.save(existSubject);
    }

    public void delete(Long id) throws SubjectNotFoundException {
        Subject existSubject = findById(id);
        logger.info("Deleting subject: " + existSubject.getName());
        subjectRepository.deleteById(id);
    }

    public long getTotal(){
        return subjectRepository.count();
    }
}
