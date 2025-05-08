package com.fabiocondo.service.impl;

import com.fabiocondo.domain.*;
import com.fabiocondo.exception.domain.ArticleNotFoundException;
import com.fabiocondo.exception.domain.UserNotFoundException;
import com.fabiocondo.repository.LikeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class LikeService {
    private final LikeRepository likeRepository;

    private final ArticleServiceImpl articleService;

    private final UserServiceImpl userService;

    public LikeService(LikeRepository likeRepository, ArticleServiceImpl articleService, UserServiceImpl userService) {
        this.likeRepository = likeRepository;
        this.articleService = articleService;
        this.userService = userService;
    }

    public Like toggleLike(Long articleId) throws ArticleNotFoundException, UserNotFoundException {
        Article article = articleService.findById(articleId);
        User user = userService.getAuthenticatedUser();
        Optional<Like> existingLike = likeRepository.findByArticleAndUser(article, user);
        if (existingLike.isPresent()) {
            likeRepository.delete(existingLike.get());
            return null;
        } else {
            Like like = new Like();
            like.setArticle(article);
            like.setUser(user);
            likeRepository.save(like);
            return like;
        }
    }

    public boolean isArticleLikedByUser(Long articleId) throws UserNotFoundException {
        User user = userService.getAuthenticatedUser();
        return likeRepository.existsByArticleIdAndUserId(articleId, user.getId());
    }

    public Long countLikesByArticleId(Long articleId) {
        return likeRepository.countLikesByArticleId(articleId);
    }

    public Page<Like> getLikesByArticleId(Long articleId, Pageable pageable) {
        return likeRepository.findByArticleId(articleId, pageable);
    }
}
