package com.fabiocondo.repository;

import com.fabiocondo.domain.Competition;
import com.fabiocondo.domain.Submission;
import com.fabiocondo.domain.User;
import com.fabiocondo.repository.query.SubmissionRepositoryQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubmissionRepository extends JpaRepository<Submission, Long>, SubmissionRepositoryQuery {
    boolean existsByCompetitionAndUser(Competition competition, User user);
    Optional<Submission> findByUserIdAndCompetitionId(Long userId, Long competitionId);
    Page<Submission> findAllByCompetitionId(Long competitionId, Pageable pageable);
    long countByCompetitionId(Long competitionId);

    /*
    @Query(
            value = "SELECT s.user_id AS userId, u.name AS userName, " +
                    "SUM(CASE WHEN a.is_correct THEN 1 ELSE 0 END) AS score, " +
                    "MIN(s.submission_time) AS submissionTime " +
                    "FROM submission s " +
                    "JOIN answer a ON a.submission_id = s.id " +
                    "JOIN users u ON u.id = s.user_id " +
                    "WHERE s.competition_id = :competitionId " +
                    "GROUP BY s.user_id, u.name " +
                    "ORDER BY score DESC, submissionTime ASC " +
                    "LIMIT :limit OFFSET :offset",
            nativeQuery = true
    )
    List<RankingDTO> getRankingPage(
            @Param("competitionId") Long competitionId,
            @Param("limit") int limit,
            @Param("offset") int offset
    );

    @Query(
            value = "SELECT COUNT(DISTINCT s.user_id) " +
                    "FROM submission s " +
                    "WHERE s.competition_id = :competitionId",
            nativeQuery = true
    )
    long countDistinctUsersByCompetition(@Param("competitionId") Long competitionId);
    */
}
