package com.fabiocondo.controller;

import com.fabiocondo.dto.UserDashboardDTO;
import com.fabiocondo.exception.domain.UserNotFoundException;
import com.fabiocondo.service.impl.UserDashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

//@RestController("/dashboard")
@RestController
public class UserDashboardController {

    private final UserDashboardService userDashboardService;

    public UserDashboardController(UserDashboardService userDashboardService) {
        this.userDashboardService = userDashboardService;
    }

    @GetMapping("/user/{userId}/dashboard")
    public UserDashboardDTO getUserDashboard(@PathVariable Long userId) throws UserNotFoundException {
        return userDashboardService.getUserDashboard(userId);
    }
}

