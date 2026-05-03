package com.fabiocondo.controller;

import com.fabiocondo.domain.UserSubjectScore;
import com.fabiocondo.dto.UserSubjectRankingDTO;
import com.fabiocondo.dto.UserSubjectRankingSummaryDTO;
import com.fabiocondo.dtoMapper.RankingMapper;
import com.fabiocondo.exception.domain.SubjectNotFoundException;
import com.fabiocondo.exception.domain.UserNotFoundException;
import com.fabiocondo.service.impl.RankingService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ranking")
public class RankingController {

    private final RankingService rankingService;

    public RankingController(RankingService rankingService) {
        this.rankingService = rankingService;
    }

    @GetMapping("/subject/{subjectId}")
    public Page<UserSubjectRankingDTO> getRanking(
            @PathVariable String subjectId,
            Pageable pageable) {

        Page<UserSubjectScore> page =
                rankingService.getRanking(subjectId, pageable);

        return page.map(RankingMapper::toDTO);
    }

    @GetMapping("/subjects/{subjectId}/users/{userId}/summary")
    public ResponseEntity<UserSubjectRankingSummaryDTO> getSummary(
            @PathVariable String subjectId,
            @PathVariable Long userId) throws SubjectNotFoundException, UserNotFoundException {

        return ResponseEntity.ok(
                rankingService.getRankingSummary(userId, subjectId)
        );
    }
}