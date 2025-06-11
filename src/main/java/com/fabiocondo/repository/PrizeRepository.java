package com.fabiocondo.repository;

import com.fabiocondo.domain.Prize;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrizeRepository extends JpaRepository<Prize, Long> {

    List<Prize> findByCompetitionIdOrderByPositionAsc(Long competitionId);

    boolean existsByCompetitionIdAndPosition(Long competitionId, Integer position);
}

