package com.fabiocondo.service.impl;

import com.fabiocondo.domain.*;
import com.fabiocondo.exception.domain.OnlineCourseNotFoundException;
import com.fabiocondo.exception.domain.UserNotFoundException;
import com.fabiocondo.repository.OnlineCourseLikeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class OnlineCourseLikeService {
    private final OnlineCourseLikeRepository onlineCourseLikeRepository;

    private final OnlineCourseService onlineCourseService;

    private final UserServiceImpl userService;

    public OnlineCourseLikeService(OnlineCourseLikeRepository onlineCourseLikeRepository, OnlineCourseService onlineCourseService, UserServiceImpl userService) {
        this.onlineCourseLikeRepository = onlineCourseLikeRepository;
        this.onlineCourseService = onlineCourseService;
        this.userService = userService;
    }

    public OnlineCourseLike toggleLike(Long onlineCourseId) throws UserNotFoundException, OnlineCourseNotFoundException {
        OnlineCourse onlineCourse = onlineCourseService.findById(onlineCourseId);
        User user = userService.getAuthenticatedUser();
        Optional<OnlineCourseLike> existingLike = onlineCourseLikeRepository.findByOnlineCourseAndUser(onlineCourse, user);
        if (existingLike.isPresent()) {
            onlineCourseLikeRepository.delete(existingLike.get());
            return null;
        } else {
            OnlineCourseLike like = new OnlineCourseLike();
            like.setOnlineCourse(onlineCourse);
            like.setUser(user);
            onlineCourseLikeRepository.save(like);
            return like;
        }
    }

    public boolean isOnlineCourseLikedByUser(Long onlineCourseId) throws UserNotFoundException {
        User user = userService.getAuthenticatedUser();
        return onlineCourseLikeRepository.existsByOnlineCourseIdAndUserId(onlineCourseId, user.getId());
    }

    public Long countLikesByOnlineCourseId(Long onlineCourseId) {
        return onlineCourseLikeRepository.countLikesByOnlineCourseId(onlineCourseId);
    }

    public Page<OnlineCourseLike> getLikesByOnlineCourseId(Long onlineCourseId, Pageable pageable) {
        return onlineCourseLikeRepository.findByOnlineCourseId(onlineCourseId, pageable);
    }
}
