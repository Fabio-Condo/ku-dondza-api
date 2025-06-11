package com.fabiocondo.service.impl;

import com.fabiocondo.domain.Competition;
import com.fabiocondo.domain.Prize;
import com.fabiocondo.exception.domain.PrizeNotFoundException;
import com.fabiocondo.repository.CompetitionRepository;
import com.fabiocondo.repository.PrizeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PrizeService {

    private final PrizeRepository prizeRepository;

    public PrizeService(PrizeRepository prizeRepository) {
        this.prizeRepository = prizeRepository;
    }

    public List<Prize> getPrizesByCompetition(Long competitionId) {
        return prizeRepository.findByCompetitionIdOrderByPositionAsc(competitionId);
    }

    public void deletePrize(Long prizeId) {
        prizeRepository.deleteById(prizeId);
    }
}

