package com.fabiocondo.controller;

import com.fabiocondo.domain.Comment;
import com.fabiocondo.domain.HttpResponse;
import com.fabiocondo.dto.CommentDTO;
import com.fabiocondo.dtoMapper.CommentMapper;
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
    private final CommentMapper commentMapper;

    public CommentController(CommentService commentService, CommentMapper commentMapper) {
        this.commentService = commentService;
        this.commentMapper = commentMapper;
    }

    @PostMapping
    public ResponseEntity<CommentDTO> addComment(@RequestBody Comment comment) {
        return ResponseEntity.status(HttpStatus.OK).body(commentMapper.domainToDTO(commentService.save(comment)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CommentDTO> updateComment(@PathVariable("id") Long id, @RequestBody Comment comment) throws CommentNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(commentMapper.domainToDTO(commentService.update(comment, id)));
    }

    @GetMapping("/question/{questionId}")
    public Page<CommentDTO> getComments(@PathVariable Long questionId, @RequestParam("currentUserId") Long currentUserId, Pageable pageable) {
        return commentMapper.domainPageToDTOPage(commentService.getCommentsByQuestion(questionId, pageable), currentUserId, pageable);
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

