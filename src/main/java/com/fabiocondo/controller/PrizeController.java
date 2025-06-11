package com.fabiocondo.controller;

import com.fabiocondo.domain.Prize;
import com.fabiocondo.exception.domain.PrizeNotFoundException;
import com.fabiocondo.service.impl.PrizeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/prizes")
public class PrizeController {

    private final PrizeService prizeService;

    public PrizeController(PrizeService prizeService) {
        this.prizeService = prizeService;
    }

    @GetMapping("/competition/{competitionId}")
    public ResponseEntity<List<Prize>> getPrizesByCompetition(@PathVariable Long competitionId) {
        return ResponseEntity.ok(prizeService.getPrizesByCompetition(competitionId));
    }


    @DeleteMapping("/{prizeId}")
    public ResponseEntity<Void> deletePrize(@PathVariable Long prizeId) {
        prizeService.deletePrize(prizeId);
        return ResponseEntity.noContent().build();
    }
}

