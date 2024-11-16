package com.fabiocondo.controller;

import com.fabiocondo.domain.PostOption;
import com.fabiocondo.domain.User;
import com.fabiocondo.exception.domain.PostOptionNotFoundException;
import com.fabiocondo.service.impl.PostOptionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/post_options")
public class PostOptionController {

    private final PostOptionService postOptionService;

    public PostOptionController(PostOptionService postOptionService) {
        this.postOptionService = postOptionService;
    }

    @GetMapping("/{optionId}/people")
    public Page<User> getPeopleWhoSelectedByOptionId(@PathVariable Long optionId, Pageable pageable) throws PostOptionNotFoundException {
        return postOptionService.getPeopleWhoSelectedByOptionId(optionId, pageable);
    }

    @PostMapping("/{optionId}/people/{userId}")
    public ResponseEntity<PostOption> addUserToOption(@PathVariable Long optionId, @PathVariable Long userId) throws PostOptionNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(postOptionService.addUserToOption(optionId, userId));
    }

    @GetMapping("/{optionId}/people/total")
    public ResponseEntity<Long> countPeopleWhoSelectedByOptionId(@PathVariable Long optionId){
        return ResponseEntity.status(HttpStatus.OK).body(postOptionService.countPeopleWhoSelectedByOptionId(optionId));
    }

    @GetMapping("/{optionId}/people/contains/{userId}")
    public ResponseEntity<Boolean> checkIfSelected(@PathVariable Long optionId, @PathVariable Long userId) {
        boolean selected = postOptionService.checkIfSelected(optionId, userId);
        return ResponseEntity.status(HttpStatus.OK).body(selected);
    }
}
