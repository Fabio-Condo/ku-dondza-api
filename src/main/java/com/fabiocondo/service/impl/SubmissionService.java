package com.fabiocondo.service.impl;

import com.fabiocondo.domain.Submission;
import com.fabiocondo.exception.domain.SubmissionNotFoundException;
import com.fabiocondo.repository.SubmissionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class SubmissionService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final SubmissionRepository submissionRepository;

    public SubmissionService(SubmissionRepository submissionRepository) {
        this.submissionRepository = submissionRepository;
    }

    public Submission findById(Long id) throws SubmissionNotFoundException {
        logger.info("Getting submission by id: " + id);
        return submissionRepository.findById(id)
                .orElseThrow(() -> new SubmissionNotFoundException("No submission found by id: " + id));
    }

    public Submission create(Submission submission) {
        submission.setSubmittedAt(new Date());
        return submissionRepository.save(submission);
    }
}
