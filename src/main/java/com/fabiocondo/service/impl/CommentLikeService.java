package com.fabiocondo.service.impl;

import com.fabiocondo.domain.Comment;
import com.fabiocondo.domain.CommentLike;
import com.fabiocondo.domain.User;
import com.fabiocondo.exception.domain.CommentNotFoundException;
import com.fabiocondo.exception.domain.UserNotFoundException;
import com.fabiocondo.repository.CommentLikeRepository;
import com.fabiocondo.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CommentLikeService {

    @Autowired
    private CommentLikeRepository commentLikeRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserServiceImpl userService;

    public CommentLike toggleLike(Long commentId) throws CommentNotFoundException, UserNotFoundException {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException("Comment not found"));
        User user = userService.getAuthenticatedUser();
        Optional<CommentLike> existingLike = commentLikeRepository.findByCommentAndUser(comment, user);

        if (existingLike.isPresent()) {
            commentLikeRepository.delete(existingLike.get());
            return null;
        } else {
            CommentLike like = new CommentLike();
            like.setComment(comment);
            like.setUser(user);
            commentLikeRepository.save(like);
            return like;
        }
    }

    public boolean isCommentLikedByUser(Long commentId) throws UserNotFoundException {
        User user = userService.getAuthenticatedUser();
        return commentLikeRepository.existsByCommentIdAndUserId(commentId, user.getId());
    }

    public Long countLikesByCommentId(Long commentId) {
        return commentLikeRepository.countByCommentId(commentId);
    }

    public Page<CommentLike> getLikesByCommentId(Long commentId, Pageable pageable) {
        return commentLikeRepository.findByCommentId(commentId, pageable);
    }
}
