package com.fabiocondo.service.impl;

import com.fabiocondo.domain.*;
import com.fabiocondo.exception.domain.BlogNotFoundException;
import com.fabiocondo.exception.domain.UserNotFoundException;
import com.fabiocondo.repository.BlogLikeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BlogLikeService {
    private final BlogLikeRepository blogLikeRepository;

    private final BlogServiceImpl blogService;

    private final UserServiceImpl userService;

    public BlogLikeService(BlogLikeRepository blogLikeRepository, BlogServiceImpl blogService, UserServiceImpl userService) {
        this.blogLikeRepository = blogLikeRepository;
        this.blogService = blogService;
        this.userService = userService;
    }

    public BlogLike toggleLike(Long postId) throws BlogNotFoundException, UserNotFoundException {
        Blog blog = blogService.findById(postId);
        User user = userService.getAuthenticatedUser();
        Optional<BlogLike> existingLike = blogLikeRepository.findByBlogAndUser(blog, user);
        if (existingLike.isPresent()) {
            blogLikeRepository.delete(existingLike.get());
            return null;
        } else {
            BlogLike like = new BlogLike();
            like.setBlog(blog);
            like.setUser(user);
            blogLikeRepository.save(like);
            return like;
        }
    }

    public boolean isBlogLikedByUser(Long blogId) throws UserNotFoundException {
        User user = userService.getAuthenticatedUser();
        return blogLikeRepository.existsByBlogIdAndUserId(blogId, user.getId());
    }

    public Long countLikesByBlogId(Long blogId) {
        return blogLikeRepository.countLikesByBlogId(blogId);
    }

    public Page<BlogLike> getLikesByBlogId(Long blogId, Pageable pageable) {
        return blogLikeRepository.findByBlogId(blogId, pageable);
    }
}
