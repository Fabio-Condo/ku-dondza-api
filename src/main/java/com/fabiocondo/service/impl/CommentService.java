package com.fabiocondo.service.impl;

import com.fabiocondo.domain.Comment;
import com.fabiocondo.exception.domain.CommentNotFoundException;
import com.fabiocondo.repository.CommentRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class CommentService {

    private final CommentRepository commentRepository;

    public CommentService(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    public Comment findById(Long id) throws CommentNotFoundException {
        return commentRepository.findById(id)
                .orElseThrow(() -> new CommentNotFoundException("No comment found by id: " + id));
    }

    public Comment save(Comment comment) {
        return commentRepository.save(comment);
    }

    public Comment update(Comment comment, Long id) throws CommentNotFoundException {
        Comment existComment = findById(id);
        BeanUtils.copyProperties(comment, existComment, "id", "question", "user", "createdAt");
        return commentRepository.save(existComment);
    }

    public Page<Comment> getCommentsByQuestion(Long questionId, Pageable pageable) {
        return commentRepository.findByQuestionIdOrderByCreatedAtDesc(questionId, pageable);
    }

    public void delete(Long id) throws CommentNotFoundException {
        Comment existComment = findById(id);
        commentRepository.deleteById(id);
    }
}

