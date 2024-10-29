package com.fabiocondo.repository;

import com.fabiocondo.domain.Competition;
import com.fabiocondo.domain.Question;
import com.fabiocondo.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CompetitionRepository extends JpaRepository<Competition, Long> {
    @Query("SELECT u FROM Competition c JOIN c.participants u WHERE c.id = :competitionId")
    Page<User> findParticipantsByCompetitionId(@Param("competitionId") Long competitionId, Pageable pageable);

    @Query("SELECT q FROM Competition c JOIN c.questions q WHERE c.id = :competitionId")
    Page<Question> findQuestionsByCompetitionId(@Param("competitionId") Long competitionId, Pageable pageable);
}

