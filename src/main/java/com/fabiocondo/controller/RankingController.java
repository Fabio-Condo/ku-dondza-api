package com.fabiocondo.controller;

import com.fabiocondo.domain.UserSubjectScore;
import com.fabiocondo.dto.UserSubjectRankingDTO;
import com.fabiocondo.dtoMapper.UserSubjectRankingMapper;
import com.fabiocondo.service.impl.RankingService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
            @PathVariable Long subjectId,
            Pageable pageable) {

        Page<UserSubjectScore> page =
                rankingService.getRanking(subjectId, pageable);

        return page.map(UserSubjectRankingMapper::toDTO);
    }
}