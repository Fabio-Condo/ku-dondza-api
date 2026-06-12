package com.fabiocondo.controller;

import com.fabiocondo.dto.FlashCardDeckResponse;
import com.fabiocondo.dto.FlashCardProgressRequest;
import com.fabiocondo.dto.FlashCardResponse;
import com.fabiocondo.dto.SaveFlashCardRequest;
import com.fabiocondo.service.impl.FlashCardServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/flash-cards")
public class FlashCardController {

    private final FlashCardServiceImpl flashCardService;

    public FlashCardController(FlashCardServiceImpl flashCardService) {
        this.flashCardService = flashCardService;
    }

    @GetMapping("/topic/{topicId}")
    public ResponseEntity<FlashCardDeckResponse> getDeck(@PathVariable Long topicId, @RequestParam Long userId) {
        return ResponseEntity.ok(flashCardService.getDeck(topicId, userId));
    }

    @PutMapping("/progress")
    public ResponseEntity<Void> updateProgress(@RequestBody FlashCardProgressRequest request, Principal principal) {

        Long userId = Long.parseLong(principal.getName());

        flashCardService.updateProgress(userId, request);

        return ResponseEntity.ok().build();
    }

    @PutMapping("/save")
    public ResponseEntity<Void> saveCard(@RequestBody SaveFlashCardRequest request, Principal principal) {

        Long userId = Long.parseLong(principal.getName());

        flashCardService.saveFlashCard(userId, request);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/saved")
    public ResponseEntity<List<FlashCardResponse>> getSavedCards(Principal principal) {

        Long userId = Long.parseLong(principal.getName());

        return ResponseEntity.ok(flashCardService.getSavedCards(userId));
    }
}
