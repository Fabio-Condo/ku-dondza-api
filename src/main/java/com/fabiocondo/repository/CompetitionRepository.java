package com.fabiocondo.repository;

import com.fabiocondo.domain.Competition;
import com.fabiocondo.domain.Question;
import com.fabiocondo.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CompetitionRepository extends JpaRepository<Competition, Long> {
    @Query("SELECT c FROM Competition c WHERE c.title LIKE %:searchParam%")
    public Page<Competition> findAll(@Param("searchParam") String searchParam, Pageable pageable);

    @Query("SELECT u FROM Competition c JOIN c.participants u WHERE c.id = :competitionId")
    Page<User> findParticipantsByCompetitionId(@Param("competitionId") Long competitionId, Pageable pageable);

    @Query("SELECT u FROM Competition c JOIN c.participationRequests u WHERE c.id = :competitionId")
    Page<User> findParticipationRequestsByCompetitionId(@Param("competitionId") Long competitionId, Pageable pageable);

    @Query("SELECT q FROM Competition c JOIN c.questions q WHERE c.id = :competitionId")
    Page<Question> findQuestionsByCompetitionId(@Param("competitionId") Long competitionId, Pageable pageable);

    @Query("SELECT COUNT(u) FROM Competition c JOIN c.participants u WHERE c.id = :competitionId")
    Long countParticipantsByCompetitionId(@Param("competitionId") Long competitionId);

    @Query("SELECT COUNT(q) FROM Competition c JOIN c.questions q WHERE c.id = :competitionId")
    Long countQuestionsByCompetitionId(@Param("competitionId") Long competitionId);

    Optional<Competition> findCompetitionByCompetitionId(String competitionId);

}

