package com.fabiocondo.service.impl;

import com.fabiocondo.domain.Prize;
import com.fabiocondo.domain.PrizeAssignment;
import com.fabiocondo.domain.User;
import com.fabiocondo.exception.domain.PrizeAlreadyAssignedException;
import com.fabiocondo.exception.domain.PrizeNotFoundException;
import com.fabiocondo.exception.domain.UserNotFoundException;
import com.fabiocondo.repository.PrizeAssignmentRepository;
import com.fabiocondo.repository.PrizeRepository;
import com.fabiocondo.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PrizeAssignmentService {

    private final PrizeAssignmentRepository prizeAssignmentRepository;
    private final PrizeRepository prizeRepository;
    private final UserRepository userRepository;

    public PrizeAssignmentService(PrizeAssignmentRepository prizeAssignmentRepository, PrizeRepository prizeRepository, UserRepository userRepository) {
        this.prizeAssignmentRepository = prizeAssignmentRepository;
        this.prizeRepository = prizeRepository;
        this.userRepository = userRepository;
    }

    public PrizeAssignment assignPrize(Long prizeId, Long userId) throws UserNotFoundException, PrizeNotFoundException, PrizeAlreadyAssignedException {
        Prize prize = prizeRepository.findById(prizeId)
                .orElseThrow(() -> new PrizeNotFoundException("Prize not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (prizeAssignmentRepository.findByPrizeId(prizeId).isPresent()) {
            throw new PrizeAlreadyAssignedException("Prize already assigned");
        }

        PrizeAssignment assignment = new PrizeAssignment();
        assignment.setPrize(prize);
        assignment.setUser(user);
        assignment.setAssignedAt(LocalDateTime.now());

        return prizeAssignmentRepository.save(assignment);
    }

    public List<PrizeAssignment> getAssignmentsByCompetition(Long competitionId) {
        return prizeAssignmentRepository.findByPrizeCompetitionId(competitionId);
    }

    public List<PrizeAssignment> getAssignmentsByUser(Long userId) {
        return prizeAssignmentRepository.findByUserId(userId);
    }

    public void removeAssignment(Long assignmentId) {
        prizeAssignmentRepository.deleteById(assignmentId);
    }
}
