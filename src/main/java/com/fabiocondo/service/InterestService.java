package com.fabiocondo.service;

import com.fabiocondo.domain.Interest;
import com.fabiocondo.exception.domain.InterestNotFoundException;

import java.util.List;

public interface InterestService {
    Interest findById(Long id) throws InterestNotFoundException;

    List<Interest> findAll();

    Interest save(Interest interest);
}
