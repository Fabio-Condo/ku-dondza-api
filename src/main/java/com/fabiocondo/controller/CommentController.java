package com.fabiocondo.controller;

import com.fabiocondo.domain.Comment;
import com.fabiocondo.exception.domain.CommentNotFoundException;
import com.fabiocondo.exception.domain.PostNotFoundException;
import com.fabiocondo.exception.domain.UserNotFoundException;
import com.fabiocondo.service.impl.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comments")
public class CommentController {

    @Autowired
    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping
    public ResponseEntity<Comment> createComment(@RequestBody Comment comment) throws UserNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(commentService.saveComment(comment));
    }

    @PostMapping("/reply-comment")
    public ResponseEntity<Comment> createComment(@RequestParam("postId") Long postId,
                                                 @RequestParam("parentCommentId") Long parentCommentId,
                                                 @RequestParam("content") String content) throws PostNotFoundException, CommentNotFoundException, UserNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(commentService.saveComment(postId, parentCommentId, content));
    }

    @GetMapping("/post2/{postId}")
    public ResponseEntity<List<Comment>> getCommentsByPostId(@PathVariable Long postId) {
        return ResponseEntity.status(HttpStatus.OK).body(commentService.getCommentsByPostId(postId));
    }

    @GetMapping("/post/{postId}")
    public Page<Comment> getCommentsByPostId(@PathVariable Long postId, Pageable pageable) {
        return commentService.getCommentsByPostId(postId, pageable);
    }

    @GetMapping("/count/{postId}")
    public Long countCommentsByPostId(@PathVariable Long postId) {
        return commentService.countCommentsByPostId(postId);
    }
}
