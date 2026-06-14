package com.fabiocondo.controller;

import com.fabiocondo.domain.FlashCard;
import com.fabiocondo.dto.FlashCardDeckResponse;
import com.fabiocondo.dto.FlashCardResponse;
import com.fabiocondo.dto.PageResponse;
import com.fabiocondo.exception.domain.QuestionNotFoundException;
import com.fabiocondo.repository.filter.FlashCardFilter;
import com.fabiocondo.service.impl.FlashCardServiceImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/flash-cards")
public class FlashCardController {

    private final FlashCardServiceImpl flashCardService;

    public FlashCardController(FlashCardServiceImpl flashCardService) {
        this.flashCardService = flashCardService;
    }

    @GetMapping("/filter")
    public PageResponse<FlashCardResponse> filter(FlashCardFilter flashCardFilter, Pageable pageable) {
        return flashCardService.filter(flashCardFilter, pageable);
    }

    @GetMapping("/topic/{topicId}")
    public ResponseEntity<FlashCardDeckResponse> getDeck(@PathVariable Long topicId) {
        return ResponseEntity.ok(flashCardService.getDeck(topicId));
    }

    @PostMapping
    public ResponseEntity<FlashCard> save(@RequestBody FlashCard question) {
        FlashCard flashCard = flashCardService.save(question);
        return ResponseEntity.ok(flashCard);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FlashCard> update(@PathVariable("id") Long id, @RequestBody FlashCard question) throws QuestionNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(flashCardService.update(question, id));
    }
}
