package com.fabiocondo.repository;

import com.fabiocondo.domain.UserSubjectScore;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import com.fabiocondo.domain.User;
import com.fabiocondo.domain.Subject;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserSubjectScoreRepository extends JpaRepository<UserSubjectScore, Long> {

    // Buscar score por user + disciplina
    Optional<UserSubjectScore> findByUserAndSubject(User user, Subject subject);

    @Query("SELECT COALESCE(SUM(u.score), 0) " +
            "FROM UserSubjectScore u " +
            "WHERE u.subject.id = :subjectId")
    Long getTotalScoreBySubject(@Param("subjectId") Long subjectId);

    @Query("SELECT COALESCE(AVG(u.score), 0) " +
            "FROM UserSubjectScore u " +
            "WHERE u.subject.id = :subjectId")
    Double getAverageScoreBySubject(@Param("subjectId") Long subjectId);

    // Posição do user no ranking
    @Query("SELECT COUNT(u) + 1 FROM UserSubjectScore u " +
            "WHERE u.subject.id = :subjectId " +
            "AND u.score > :score")
    Long getUserRank(@Param("subjectId") Long subjectId,
                     @Param("score") Long score);

    @Query("SELECT u FROM UserSubjectScore u " +
            "WHERE u.subject.subjectId = :subjectId " +
            "ORDER BY u.score DESC")
    Page<UserSubjectScore> findRankingBySubjectId(
            @Param("subjectId") String subjectId,
            Pageable pageable);

    // Ranking completo da disciplina
    //List<UserSubjectScore> findBySubjectOrderByScoreDesc(Subject subject);

    // Top 10 da disciplina
    //List<UserSubjectScore> findTop10BySubjectOrderByScoreDesc(Subject subject);

    // Ranking por subjectId (JPQL compatível Java 8)
    //@Query("SELECT u FROM UserSubjectScore u " +
    //        "WHERE u.subject.id = :subjectId " +
    //        "ORDER BY u.score DESC")
    //List<UserSubjectScore> getRankingBySubjectId(@Param("subjectId") Long subjectId);
}
