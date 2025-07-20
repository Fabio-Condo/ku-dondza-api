package com.fabiocondo.service.impl;

import com.fabiocondo.domain.*;
import com.fabiocondo.exception.domain.ArticleNotFoundException;
import com.fabiocondo.exception.domain.UserNotFoundException;
import com.fabiocondo.repository.LikeRepository;
import com.fabiocondo.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class LikeService {
    private final LikeRepository likeRepository;

    private final ArticleServiceImpl articleService;

    private final UserRepository userRepository;

    public LikeService(LikeRepository likeRepository, ArticleServiceImpl articleService, UserRepository userRepository) {
        this.likeRepository = likeRepository;
        this.articleService = articleService;
        this.userRepository = userRepository;
    }

    public Like toggleLike(Long articleId, Long currentUserId) throws ArticleNotFoundException, UserNotFoundException {
        Article article = articleService.findById(articleId);
        User currentUser = userRepository.findById(currentUserId).orElseThrow(null);
        Optional<Like> existingLike = likeRepository.findByArticleAndUser(article, currentUser);
        if (existingLike.isPresent()) {
            likeRepository.delete(existingLike.get());
            return null;
        } else {
            Like like = new Like();
            like.setArticle(article);
            like.setUser(currentUser);
            likeRepository.save(like);
            return like;
        }
    }

    public boolean isArticleLikedByUser(Long articleId, Long currentUserId) throws UserNotFoundException {
        //User currentUser = userService.getAuthenticatedUser();
        User currentUser = userRepository.findById(currentUserId).orElseThrow(null);
        return likeRepository.existsByArticleIdAndUserId(articleId, currentUser.getId());
    }

    public Long countLikesByArticleId(Long articleId) {
        return likeRepository.countLikesByArticleId(articleId);
    }

    public Page<Like> getLikesByArticleId(Long articleId, Pageable pageable) {
        return likeRepository.findByArticleId(articleId, pageable);
    }
}
