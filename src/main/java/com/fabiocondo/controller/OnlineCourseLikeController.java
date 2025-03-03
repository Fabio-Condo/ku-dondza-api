package com.fabiocondo.controller;

import com.fabiocondo.domain.OnlineCourseLike;
import com.fabiocondo.exception.domain.OnlineCourseNotFoundException;
import com.fabiocondo.exception.domain.UserNotFoundException;
import com.fabiocondo.service.impl.OnlineCourseLikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/online-course-likes")
public class OnlineCourseLikeController {

    @Autowired
    private OnlineCourseLikeService onlineCourseLikeService;

    @PostMapping("/course/{onlineCourseId}")
    public ResponseEntity<OnlineCourseLike> toggleLike(@PathVariable Long onlineCourseId) throws UserNotFoundException, OnlineCourseNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(onlineCourseLikeService.toggleLike(onlineCourseId));
    }

    @GetMapping("/course/{onlineCourseId}/check")
    public ResponseEntity<Boolean> checkIfLiked(@PathVariable Long onlineCourseId) throws UserNotFoundException {
        boolean liked = onlineCourseLikeService.isOnlineCourseLikedByUser(onlineCourseId);
        return ResponseEntity.ok(liked);
    }

    @GetMapping("/course/{onlineCourseId}")
    public Page<OnlineCourseLike> getLikesByOnlineCourseId(@PathVariable Long onlineCourseId, Pageable pageable) {
        return onlineCourseLikeService.getLikesByOnlineCourseId(onlineCourseId, pageable);
    }

    @GetMapping("/count/{onlineCourseId}")
    public Long countLikesByOnlineCourseId(@PathVariable Long onlineCourseId) {
        return onlineCourseLikeService.countLikesByOnlineCourseId(onlineCourseId);
    }
}
