package com.fabiocondo.controller;

import com.fabiocondo.domain.CommentLike;
import com.fabiocondo.exception.domain.CommentNotFoundException;
import com.fabiocondo.service.impl.CommentLikeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/comment-likes")
public class CommentLikeController {

    private final CommentLikeService commentLikeService;

    public CommentLikeController(CommentLikeService commentLikeService) {
        this.commentLikeService = commentLikeService;
    }

    @PostMapping("/comments/{commentId}/users/{currentUserId}")
    public ResponseEntity<CommentLike> toggleLike(@PathVariable Long commentId, @PathVariable("currentUserId") Long currentUserId) throws CommentNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(commentLikeService.toggleLike(commentId, currentUserId));
    }

    @GetMapping("/comments/{commentId}")
    public Page<CommentLike> getLikesByCommentId(@PathVariable Long commentId, Pageable pageable) {
        return commentLikeService.getLikesByCommentId(commentId, pageable);
    }

    @GetMapping("/count/{commentId}")
    public Long countLikesByCommentId(@PathVariable Long commentId) {
        return commentLikeService.countLikesByCommentId(commentId);
    }
}
