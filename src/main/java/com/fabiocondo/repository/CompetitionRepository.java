package com.fabiocondo.repository;

import com.fabiocondo.domain.*;
import com.fabiocondo.repository.query.CompetitionRepositoryQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CompetitionRepository extends JpaRepository<Competition, Long>, CompetitionRepositoryQuery {
    @Query("SELECT c FROM Competition c WHERE c.competitionType LIKE %:searchParam%")
    public Page<Competition> findAll(@Param("searchParam") String searchParam, Pageable pageable);

    @Query("SELECT q FROM Competition c JOIN c.questions q WHERE c.id = :competitionId")
    Page<Question> findQuestionsByCompetitionId(@Param("competitionId") Long competitionId, Pageable pageable);

    @Query("SELECT COUNT(q) FROM Competition c JOIN c.questions q WHERE c.id = :competitionId")
    Long countQuestionsByCompetitionId(@Param("competitionId") Long competitionId);

    Optional<Competition> findCompetitionByCompetitionId(String competitionId);

    long countByQuestions(Question question);

    public Page<Competition> findAllByQuestions(Question question, Pageable pageable);

    @Query("SELECT u FROM Competition c JOIN c.allowedUsers u WHERE c.id = :competitionId")
    Page<User> findAllowedUsersByCompetitionId(@Param("competitionId") Long competitionId, Pageable pageable);

    long countAllowedUsersBByCompetitionId(@Param("competitionId") Long competitionId);

}