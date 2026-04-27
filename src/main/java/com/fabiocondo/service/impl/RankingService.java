package com.fabiocondo.service.impl;

import com.fabiocondo.domain.Subject;
import com.fabiocondo.domain.User;
import com.fabiocondo.domain.UserSubjectScore;
import com.fabiocondo.dto.UserSubjectRankingSummaryDTO;
import com.fabiocondo.repository.SubjectRepository;
import com.fabiocondo.repository.UserRepository;
import com.fabiocondo.repository.UserSubjectScoreRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class RankingService {

    private final UserSubjectScoreRepository userSubjectScoreRepository;

    private final UserRepository userRepository;
    private final SubjectRepository subjectRepository;

    private final UserSubjectScoreService userSubjectScoreService;


    public RankingService(UserSubjectScoreRepository userSubjectScoreRepository, UserRepository userRepository, SubjectRepository subjectRepository, UserSubjectScoreService userSubjectScoreService) {
        this.userSubjectScoreRepository = userSubjectScoreRepository;
        this.userRepository = userRepository;
        this.subjectRepository = subjectRepository;
        this.userSubjectScoreService = userSubjectScoreService;
    }

    public Page<UserSubjectScore> getRanking(Long subjectId, Pageable pageable) {
        return userSubjectScoreRepository.findRankingBySubjectId(subjectId, pageable);
    }

    public UserSubjectRankingSummaryDTO getRankingSummary(Long userId, Long subjectId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new RuntimeException("Disciplina não encontrada"));

        Long currentUserRanking =
                userSubjectScoreService.getUserRank(user, subject);

        Long currentUserScore =
                userSubjectScoreService.getScore(user, subject);

        UserSubjectRankingSummaryDTO dto = new UserSubjectRankingSummaryDTO();
        dto.setSubjectId(subject.getSubjectId());
        dto.setSubjectName(subject.getName());

        dto.setUserId(user.getId());
        dto.setFullName(user.getFullName());
        dto.setProfileImageUrl(user.getProfileImageUrl());

        dto.setCurrentUserRank(currentUserRanking);
        dto.setCurrentUserScore(currentUserScore);
        dto.setTotalTopics(subject.getTopics().size());

        dto.setAccuracyRate(20);
        dto.setAverageScore(30);

        return dto;
    }
}
