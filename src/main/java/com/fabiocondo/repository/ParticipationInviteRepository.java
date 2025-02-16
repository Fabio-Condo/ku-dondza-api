package com.fabiocondo.repository;

import com.fabiocondo.domain.ParticipationInvite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ParticipationInviteRepository extends JpaRepository<ParticipationInvite, Long> {

    boolean existsByCompetitionIdAndUserId(Long competitionId, Long userId);
    Optional<ParticipationInvite>  findByCompetitionIdAndUserId(Long competitionId, Long userId);
}
