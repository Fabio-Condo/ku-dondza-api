package com.fabiocondo.controller;

import com.fabiocondo.domain.PollOption;
import com.fabiocondo.domain.User;
import com.fabiocondo.exception.domain.PollOptionNotFoundException;
import com.fabiocondo.service.impl.PollOptionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/poll_options")
public class PollOptionController {

    private final PollOptionService pollOptionService;

    public PollOptionController(PollOptionService pollOptionService) {
        this.pollOptionService = pollOptionService;
    }

    @GetMapping("/{optionId}/people")
    public Page<User> getPeopleWhoSelectedByOptionId(@PathVariable Long optionId, Pageable pageable) throws PollOptionNotFoundException {
        return pollOptionService.getPeopleWhoSelectedByOptionId(optionId, pageable);
    }

    @PostMapping("/{optionId}/people/{userId}")
    public ResponseEntity<PollOption> addUserToOption(@PathVariable Long optionId, @PathVariable Long userId) throws PollOptionNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(pollOptionService.addUserToOption(optionId, userId));
    }

    @PostMapping("/{optionId}/people/{userId}/vote")
    public ResponseEntity<PollOption> toggleUserVote(@PathVariable Long optionId, @PathVariable Long userId) throws PollOptionNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(pollOptionService.toggleUserVote(optionId, userId));
    }

    @GetMapping("/{optionId}/people/total")
    public ResponseEntity<Long> countPeopleWhoSelectedByOptionId(@PathVariable Long optionId){
        return ResponseEntity.status(HttpStatus.OK).body(pollOptionService.countPeopleWhoSelectedByOptionId(optionId));
    }

    @GetMapping("/{optionId}/people/contains/{userId}")
    public ResponseEntity<Boolean> checkIfSelected(@PathVariable Long optionId, @PathVariable Long userId) {
        boolean selected = pollOptionService.checkIfSelected(optionId, userId);
        return ResponseEntity.status(HttpStatus.OK).body(selected);
    }
}
