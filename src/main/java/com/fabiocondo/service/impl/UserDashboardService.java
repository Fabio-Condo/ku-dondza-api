package com.fabiocondo.service.impl;

import com.fabiocondo.domain.User;
import com.fabiocondo.dto.UserDashboardDTO;
import com.fabiocondo.exception.domain.UserNotFoundException;
import com.fabiocondo.repository.CompetitionRepository;
import com.fabiocondo.repository.QuizRepository;
import com.fabiocondo.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserDashboardService {

    private final CompetitionRepository competitionRepository;
    private final QuizRepository quizRepository;
    private final UserRepository userRepository;

    public UserDashboardService(CompetitionRepository competitionRepository, QuizRepository quizRepository, UserRepository userRepository) {
        this.competitionRepository = competitionRepository;
        this.quizRepository = quizRepository;
        this.userRepository = userRepository;
    }

    public UserDashboardDTO getUserDashboard(Long userId) throws UserNotFoundException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("No User found by id: " + userId));

        long totalQuizzesCreated = quizRepository.countByUser(user);
        long totalCompetitionsParticipating = competitionRepository.countByParticipants(user);

        UserDashboardDTO dashboardDTO = new UserDashboardDTO();
        dashboardDTO.setTotalQuizzesCreated(totalQuizzesCreated);
        dashboardDTO.setTotalCompetitionsParticipating(totalCompetitionsParticipating);

        return dashboardDTO;
    }
}

