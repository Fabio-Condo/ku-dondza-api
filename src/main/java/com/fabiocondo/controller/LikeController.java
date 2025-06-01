package com.fabiocondo.controller;

import com.fabiocondo.domain.Like;
import com.fabiocondo.exception.domain.ArticleNotFoundException;
import com.fabiocondo.exception.domain.UserNotFoundException;
import com.fabiocondo.service.impl.LikeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/likes")
public class LikeController {

    private final LikeService likeService;

    public LikeController(LikeService likeService) {
        this.likeService = likeService;
    }

    @PostMapping("/articles/{articleId}/users/{currentUserId}")
    public ResponseEntity<Like> toggleLike(@PathVariable Long articleId, @PathVariable("currentUserId") Long currentUserId) throws UserNotFoundException, ArticleNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(likeService.toggleLike(articleId, currentUserId));
    }

    @GetMapping("/articles/{articleId}")
    public Page<Like> getLikesByArticleId(@PathVariable Long articleId, Pageable pageable) {
        return likeService.getLikesByArticleId(articleId, pageable);
    }

    @GetMapping("/count/{articleId}")
    public Long countLikesByArticleId(@PathVariable Long articleId) {
        return likeService.countLikesByArticleId(articleId);
    }
}
