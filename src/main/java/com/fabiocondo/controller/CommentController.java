package com.fabiocondo.controller;

import com.fabiocondo.domain.Comment;
import com.fabiocondo.domain.HttpResponse;
import com.fabiocondo.exception.domain.CommentNotFoundException;
import com.fabiocondo.service.impl.CommentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping
    public Comment addComment(@RequestBody Comment comment) {
        return commentService.save(comment);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Comment> updateComment(@PathVariable("id") Long id, @RequestBody Comment comment) throws CommentNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(commentService.update(comment, id));
    }

    @GetMapping("/question/{questionId}")
    public Page<Comment> getComments(@PathVariable Long questionId, Pageable pageable) {
        return commentService.getCommentsByQuestion(questionId, pageable);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") Long id) throws CommentNotFoundException {
        commentService.delete(id);
        return response(HttpStatus.OK, "Comment deleted successfully");
    }

    private ResponseEntity<HttpResponse> response(HttpStatus httpStatus, String message) {
        return new ResponseEntity<>(
                new HttpResponse(httpStatus.value(), httpStatus, httpStatus.getReasonPhrase().toUpperCase(), message),
                httpStatus);
    }
}

