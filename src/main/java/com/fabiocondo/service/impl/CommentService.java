package com.fabiocondo.service.impl;

import com.fabiocondo.domain.Comment;
import com.fabiocondo.domain.Post;
import com.fabiocondo.exception.domain.CommentNotFoundException;
import com.fabiocondo.exception.domain.PostNotFoundException;
import com.fabiocondo.repository.CommentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private final CommentRepository commentRepository;

    @Autowired
    private PostServiceImpl postService;

    public CommentService(CommentRepository commentRepository, PostServiceImpl postService) {
        this.commentRepository = commentRepository;
        this.postService = postService;
    }

    public Comment findById(Long id) throws CommentNotFoundException {
        logger.info("Getting comment by id: " + id);
        return commentRepository.findById(id)
                .orElseThrow(() -> new CommentNotFoundException("No comment found by id: " + id));
    }

    public Comment saveComment(Comment comment) {
        return commentRepository.save(comment);
    }

    public Comment saveComment(Long postId, Long parentCommentId, String content) throws CommentNotFoundException, PostNotFoundException {
        Post post = postService.findById(postId);
        Comment parentComment = findById(parentCommentId);
        Comment comment = new Comment();
        comment.setPost(post);
        comment.setParentComment(parentComment);
        comment.setContent(content);
        return commentRepository.save(comment);
    }

    public List<Comment> getCommentsByPostId(Long postId) {
        return commentRepository.findByPostId(postId);
    }
}
