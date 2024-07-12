package com.fabiocondo.controller;

import com.fabiocondo.domain.Like;
import com.fabiocondo.exception.domain.PostNotFoundException;
import com.fabiocondo.exception.domain.UserNotFoundException;
import com.fabiocondo.service.impl.LikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/likes")
public class LikeController {
    @Autowired
    private LikeService likeService;

    @PostMapping("/posts/{postId}")
    public ResponseEntity<Like> toggleLike(@PathVariable Long postId) throws UserNotFoundException, PostNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(likeService.toggleLike(postId));
    }

    @GetMapping("/posts/{postId}/check")
    public ResponseEntity<Boolean> checkIfLiked(@PathVariable Long postId) throws UserNotFoundException {
        boolean liked = likeService.isPostLikedByUser(postId);
        return ResponseEntity.ok(liked);
    }

    @GetMapping("/count/{postId}")
    public Long countLikesByPostId(@PathVariable Long postId) {
        return likeService.countLikesByPostId(postId);
    }
}
