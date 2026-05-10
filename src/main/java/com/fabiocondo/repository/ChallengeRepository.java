package com.fabiocondo.repository;

import com.fabiocondo.domain.Challenge;
import com.fabiocondo.repository.query.ChallengeRepositoryQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ChallengeRepository extends JpaRepository<Challenge, Long>, ChallengeRepositoryQuery {

    Optional<Challenge> findByChallengeId(String challengeId);

    Page<Challenge> findAll(Pageable pageable);

    @Query("SELECT CASE WHEN COUNT(q) > 0 THEN true ELSE false END " +
            "FROM Challenge c " +
            "JOIN c.submittedChallengeQuizzes q " +
            "WHERE c.id = :challengeId " +
            "AND q.user.id = :userId")
    boolean existsQuizInChallenge(@Param("challengeId") Long challengeId, @Param("userId") Long userId);
}