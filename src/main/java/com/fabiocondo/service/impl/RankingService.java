package com.fabiocondo.service.impl;

import com.fabiocondo.domain.Subject;
import com.fabiocondo.domain.User;
import com.fabiocondo.domain.UserSubjectScore;
import com.fabiocondo.dto.UserSubjectRankingSummaryDTO;
import com.fabiocondo.exception.domain.SubjectNotFoundException;
import com.fabiocondo.exception.domain.UserNotFoundException;
import com.fabiocondo.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class RankingService {

    private final UserSubjectScoreRepository userSubjectScoreRepository;
    private final UserRepository userRepository;
    private final SubjectRepository subjectRepository;
    private final TopicRepository topicRepository;
    private final TopicTestRepository topicTestRepository;
    private final UserSubjectScoreService userSubjectScoreService;

    public RankingService(UserSubjectScoreRepository userSubjectScoreRepository, UserRepository userRepository, SubjectRepository subjectRepository, TopicRepository topicRepository, TopicTestRepository topicTestRepository, UserSubjectScoreService userSubjectScoreService) {
        this.userSubjectScoreRepository = userSubjectScoreRepository;
        this.userRepository = userRepository;
        this.subjectRepository = subjectRepository;
        this.topicRepository = topicRepository;
        this.topicTestRepository = topicTestRepository;
        this.userSubjectScoreService = userSubjectScoreService;
    }

    public Page<UserSubjectScore> getRanking(String subjectId, Pageable pageable) {
        return userSubjectScoreRepository.findRankingBySubjectId(subjectId, pageable);
    }

    public UserSubjectRankingSummaryDTO getRankingSummary(Long userId, String subjectId)
            throws SubjectNotFoundException, UserNotFoundException {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado"));

        Subject subject = subjectRepository.findSubjectBySubjectId(subjectId)
                .orElseThrow(() -> new SubjectNotFoundException("Disciplina não encontrada"));

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

        // TOPICS
        dto.setTotalTopics(
                topicRepository.countBySubjectIdAndEnabledTrue(subject.getId())
        );

        // TESTS
        dto.setTotalTests(
                topicTestRepository.countBySubjectId(subject.getId())
        );

        dto.setAverageScore(
                userSubjectScoreRepository.getAverageScoreBySubject(subject.getId())
        );

        return dto;
    }
}
