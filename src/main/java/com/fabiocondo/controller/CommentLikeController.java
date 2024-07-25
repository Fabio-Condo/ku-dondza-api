package com.fabiocondo.controller;

import com.fabiocondo.domain.CommentLike;
import com.fabiocondo.exception.domain.CommentNotFoundException;
import com.fabiocondo.exception.domain.UserNotFoundException;
import com.fabiocondo.service.impl.CommentLikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/comment-likes")
public class CommentLikeController {

    private final CommentLikeService commentLikeService;

    @Autowired
    public CommentLikeController(CommentLikeService commentLikeService) {
        this.commentLikeService = commentLikeService;
    }

    @PostMapping("/{commentId}/toggle")
    public ResponseEntity<CommentLike> toggleLike(@PathVariable Long commentId) throws UserNotFoundException, CommentNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(commentLikeService.toggleLike(commentId));
    }

    @GetMapping("/{commentId}/is-liked")
    public ResponseEntity<Boolean> isCommentLikedByUser(@PathVariable Long commentId) throws UserNotFoundException {
        boolean isLiked = commentLikeService.isCommentLikedByUser(commentId);
        return ResponseEntity.status(HttpStatus.OK).body(isLiked);
    }

    //@GetMapping("/{commentId}/likes-count")
    //public ResponseEntity<Long> getLikesCount(@PathVariable Long commentId) {
    //    Long likeCount = commentLikeService.countLikesByCommentId(commentId);
    //    return ResponseEntity.ok(likeCount);
    //}

    @GetMapping("/{commentId}/likes")
    public ResponseEntity<Page<CommentLike>> getLikesByCommentId(@PathVariable Long commentId, Pageable pageable) {
        Page<CommentLike> likes = commentLikeService.getLikesByCommentId(commentId, pageable);
        return ResponseEntity.ok(likes);
    }
}
