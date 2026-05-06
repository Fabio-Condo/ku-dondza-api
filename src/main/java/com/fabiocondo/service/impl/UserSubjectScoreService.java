package com.fabiocondo.service.impl;

import com.fabiocondo.domain.Subject;
import com.fabiocondo.domain.User;
import com.fabiocondo.domain.UserSubjectScore;
import com.fabiocondo.repository.UserSubjectScoreRepository;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class UserSubjectScoreService {

    private final UserSubjectScoreRepository userSubjectScoreRepository;

    public UserSubjectScoreService(UserSubjectScoreRepository userSubjectScoreRepository) {
        this.userSubjectScoreRepository = userSubjectScoreRepository;
    }

    public void addScore(User user, Subject subject, long points) {

        UserSubjectScore score = userSubjectScoreRepository.findByUserAndSubject(user, subject)
                .orElseGet(() -> {
                    UserSubjectScore s = new UserSubjectScore();
                    s.setUser(user);
                    s.setSubject(subject);
                    s.setScore(0L);
                    s.setTestsCompleted(0L); // importante
                    return s;
                });

        // incrementa score
        score.setScore(score.getScore() + points);

        // incrementa testes finalizados
        if (score.getTestsCompleted() == null) {
            score.setTestsCompleted(0L);
        }
        score.setTestsCompleted(score.getTestsCompleted() + 1);

        score.setUpdatedAt(new Date());

        userSubjectScoreRepository.save(score);
    }

    public Long getUserRank(User user, Subject subject) {

        UserSubjectScore score = userSubjectScoreRepository
                .findByUserAndSubject(user, subject)
                .orElse(null);

        if (score == null) {
            return null;
        }

        return userSubjectScoreRepository.getUserRank(
                subject.getId(),
                score.getScore()
        );
    }

    public Long getScore(User user, Subject subject) {

        UserSubjectScore score = userSubjectScoreRepository
                .findByUserAndSubject(user, subject)
                .orElse(null);

        return (score != null) ? score.getScore() : 0L;
    }
}
