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
                    return s;
                });

        score.setScore(score.getScore() + points);
        score.setUpdatedAt(new Date());

        userSubjectScoreRepository.save(score);
    }
}
