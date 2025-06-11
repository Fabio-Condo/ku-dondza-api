package com.fabiocondo.controller;

import com.fabiocondo.domain.PrizeAssignment;
import com.fabiocondo.exception.domain.PrizeAlreadyAssignedException;
import com.fabiocondo.exception.domain.PrizeNotFoundException;
import com.fabiocondo.exception.domain.UserNotFoundException;
import com.fabiocondo.service.impl.PrizeAssignmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/prize-assignments")
public class PrizeAssignmentController {

    private final PrizeAssignmentService assignmentService;

    public PrizeAssignmentController(PrizeAssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    @PostMapping("/assign")
    public ResponseEntity<PrizeAssignment> assignPrize(@RequestParam Long prizeId, @RequestParam Long userId) throws UserNotFoundException, PrizeAlreadyAssignedException, PrizeNotFoundException {
        return ResponseEntity.ok(assignmentService.assignPrize(prizeId, userId));
    }

    @GetMapping("/competition/{competitionId}")
    public ResponseEntity<List<PrizeAssignment>> getAssignmentsByCompetition(@PathVariable Long competitionId) {
        return ResponseEntity.ok(assignmentService.getAssignmentsByCompetition(competitionId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PrizeAssignment>> getAssignmentsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(assignmentService.getAssignmentsByUser(userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAssignment(@PathVariable Long id) {
        assignmentService.removeAssignment(id);
        return ResponseEntity.noContent().build();
    }
}
