package com.fabiocondo.repository;

import com.fabiocondo.domain.PrizeAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PrizeAssignmentRepository extends JpaRepository<PrizeAssignment, Long> {

    Optional<PrizeAssignment> findByPrizeId(Long prizeId);

    List<PrizeAssignment> findByUserId(Long userId);

    List<PrizeAssignment> findByPrizeCompetitionId(Long competitionId);
}

