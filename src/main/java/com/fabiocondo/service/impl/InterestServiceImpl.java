package com.fabiocondo.service.impl;

import com.fabiocondo.domain.Interest;
import com.fabiocondo.exception.domain.InterestNotFoundException;
import com.fabiocondo.repository.InterestRepository;
import com.fabiocondo.service.InterestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InterestServiceImpl implements InterestService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    InterestRepository interestRepository;

    public InterestServiceImpl(InterestRepository interestRepository) {
        this.interestRepository = interestRepository;
    }

    @Override
    public Interest findById(Long id) throws InterestNotFoundException {
        logger.info("Getting interest by id: " + id);
        return interestRepository.findById(id)
                .orElseThrow(() -> new InterestNotFoundException("No interest found by id: " + id));
    }

    @Override
    public List<Interest> findAll() {
        return interestRepository.findAll();
    }

    @Override
    public Interest save(Interest interest) {
        return interestRepository.save(interest);
    }
}
