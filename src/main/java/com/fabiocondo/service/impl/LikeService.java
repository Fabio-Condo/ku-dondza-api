package com.fabiocondo.service.impl;

import com.fabiocondo.domain.Like;
import com.fabiocondo.domain.Post;
import com.fabiocondo.exception.domain.PostNotFoundException;
import com.fabiocondo.exception.domain.UserNotFoundException;
import com.fabiocondo.repository.LikeRepository;
import com.fabiocondo.repository.PostRepository;
import com.fabiocondo.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class LikeService {
    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostServiceImpl postService;

    @Autowired
    private UserServiceImpl userService;

    public LikeService(LikeRepository likeRepository, PostRepository postRepository, PostServiceImpl postService, UserServiceImpl userService) {
        this.likeRepository = likeRepository;
        this.postRepository = postRepository;
        this.postService = postService;
        this.userService = userService;
    }

    public Like toggleLike(Long postId) throws PostNotFoundException, UserNotFoundException {
        Post post = postService.findById(postId);
        User user = userService.getAuthenticatedUser();
        Optional<Like> existingLike = likeRepository.findByPostAndUser(post, user);
        if (existingLike.isPresent()) {
            likeRepository.delete(existingLike.get());
            return null;
        } else {
            Like like = new Like();
            like.setPost(post);
            like.setUser(user);
            likeRepository.save(like);
            return like;
        }
    }

    public boolean isPostLikedByUser(Long postId) throws UserNotFoundException {
        User user = userService.getAuthenticatedUser();
        return likeRepository.existsByPostIdAndUserId(postId, user.getId());
    }
}
