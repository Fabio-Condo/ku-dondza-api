package com.fabiocondo.service.impl;

import com.fabiocondo.domain.*;
import com.fabiocondo.exception.domain.CommentNotFoundException;
import com.fabiocondo.repository.CommentLikeRepository;
import com.fabiocondo.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CommentLikeService {
    private final CommentLikeRepository likeRepository;

    private final CommentService commentService;

    private final UserRepository userRepository;

    public CommentLikeService(CommentLikeRepository likeRepository, CommentService commentService, UserRepository userRepository) {
        this.likeRepository = likeRepository;
        this.commentService = commentService;
        this.userRepository = userRepository;
    }

    public CommentLike toggleLike(Long commentId, Long currentUserId) throws CommentNotFoundException {
        Comment comment = commentService.findById(commentId);
        User currentUser = userRepository.findById(currentUserId).orElseThrow(null);
        Optional<CommentLike> existingLike = likeRepository.findByCommentAndUser(comment, currentUser);
        if (existingLike.isPresent()) {
            likeRepository.delete(existingLike.get());
            return null;
        } else {
            CommentLike like = new CommentLike();
            like.setComment(comment);
            like.setUser(currentUser);
            likeRepository.save(like);
            return like;
        }
    }

    public boolean isCommentLikedByUser(Long commentId, Long currentUserId) {
        User currentUser = userRepository.findById(currentUserId).orElseThrow(null);
        return likeRepository.existsByCommentIdAndUserId(commentId, currentUser.getId());
    }

    public Long countLikesByCommentId(Long commentId) {
        return likeRepository.countLikesByCommentId(commentId);
    }

    public Page<CommentLike> getLikesByCommentId(Long commentId, Pageable pageable) {
        return likeRepository.findByCommentId(commentId, pageable);
    }
}
