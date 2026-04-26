package com.fabiocondo.service.impl;

import com.fabiocondo.domain.UserSubjectScore;
import com.fabiocondo.repository.UserSubjectScoreRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class RankingService {

    private final UserSubjectScoreRepository userSubjectScoreRepository;

    public RankingService(UserSubjectScoreRepository userSubjectScoreRepository) {
        this.userSubjectScoreRepository = userSubjectScoreRepository;
    }

    public Page<UserSubjectScore> getRanking(Long subjectId, Pageable pageable) {
        return userSubjectScoreRepository.findRankingBySubjectId(subjectId, pageable);
    }
}
