package com.fabiocondo.repository;

import com.fabiocondo.domain.Competition;
import com.fabiocondo.domain.Question;
import com.fabiocondo.domain.Submission;
import com.fabiocondo.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    boolean existsByCompetitionAndUser(Competition competition, User user);
    Optional<Submission> findByUserIdAndCompetitionId(Long userId, Long competitionId);
    List<Submission> findByCompetitionId(Long competitionId);
    long countByAnswers_Question(Question question);
    public long countByAnswers_IsCorrectTrueAndAnswers_Question(Question question);
    public long countByAnswers_IsNullAndAnswers_Question(Question question);

    // Método para encontrar todas as submissões de uma questão
    //List<Submission> findByAnswersQuestionId(Long questionId);
}
