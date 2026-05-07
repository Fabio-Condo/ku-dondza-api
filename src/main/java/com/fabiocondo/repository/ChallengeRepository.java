package com.fabiocondo.repository;

import com.fabiocondo.domain.Challenge;
import com.fabiocondo.repository.query.ChallengeRepositoryQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChallengeRepository extends JpaRepository<Challenge, Long>, ChallengeRepositoryQuery {

    Optional<Challenge> findByChallengeId(String challengeId);

    Page<Challenge> findAll(Pageable pageable);
}