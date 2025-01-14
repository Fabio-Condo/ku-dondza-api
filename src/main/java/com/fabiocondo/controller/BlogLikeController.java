package com.fabiocondo.controller;

import com.fabiocondo.domain.BlogLike;
import com.fabiocondo.exception.domain.BlogNotFoundException;
import com.fabiocondo.exception.domain.UserNotFoundException;
import com.fabiocondo.service.impl.BlogLikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/blog-likes")
public class BlogLikeController {

    @Autowired
    private BlogLikeService likeService;

    @PostMapping("/blog/{blogId}")
    public ResponseEntity<BlogLike> toggleLike(@PathVariable Long blogId) throws UserNotFoundException, BlogNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(likeService.toggleLike(blogId));
    }

    @GetMapping("/blog/{blogId}/check")
    public ResponseEntity<Boolean> checkIfLiked(@PathVariable Long blogId) throws UserNotFoundException {
        boolean liked = likeService.isBlogLikedByUser(blogId);
        return ResponseEntity.ok(liked);
    }

    @GetMapping("/blog/{blogId}")
    public Page<BlogLike> getLikesByBlogId(@PathVariable Long blogId, Pageable pageable) {
        return likeService.getLikesByBlogId(blogId, pageable);
    }

    @GetMapping("/count/{blogId}")
    public Long countLikesByBlogId(@PathVariable Long blogId) {
        return likeService.countLikesByBlogId(blogId);
    }
}
