package com.fabiocondo.controller;

import com.fabiocondo.domain.Interest;
import com.fabiocondo.exception.domain.InterestNotFoundException;
import com.fabiocondo.service.impl.InterestServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/interests")
public class InterestController {

    public InterestServiceImpl interestService;

    public InterestController(InterestServiceImpl interestService) {
        this.interestService = interestService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Interest> findById(@PathVariable("id") Long id) throws InterestNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(interestService.findById(id));
    }

    @GetMapping
    public ResponseEntity<List<Interest>> findAll() throws InterestNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(interestService.findAll());
    }

    @PostMapping
    public ResponseEntity<Interest> save(@RequestBody Interest interest) throws InterestNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(interestService.save(interest));
    }
}
