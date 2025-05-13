package com.fabiocondo.dtoMapper;

import com.fabiocondo.domain.Article;
import com.fabiocondo.dto.ArticleDTO;
import com.fabiocondo.exception.domain.ArticleNotFoundException;
import com.fabiocondo.exception.domain.UserNotFoundException;
import com.fabiocondo.service.UserService;
import com.fabiocondo.service.impl.LikeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class ArticleMapper {

    private final LikeService likeService;
    private final UserService userService;

    public ArticleMapper(LikeService likeService, UserService userService) {
        this.likeService = likeService;
        this.userService = userService;
    }

    public Article dtoToDomainObject(ArticleDTO articleDTO) {
        Article article = new Article();
        article.setId(articleDTO.getId());
        article.setArticleId(articleDTO.getArticleId());
        article.setTitle(articleDTO.getTitle());
        article.setContent(articleDTO.getContent());
        article.setCategory(articleDTO.getCategory());
        article.setFileName(articleDTO.getFileName());
        article.setUrlFile(articleDTO.getUrlFile());
        article.setReadingTimeMinutes(articleDTO.getReadingTimeMinutes());
        article.setDate(articleDTO.getDate());
        article.setLastUpdated(articleDTO.getLastUpdated());
        return article;
    }

    public ArticleDTO domainToDTO(Article article) throws UserNotFoundException, ArticleNotFoundException {
        ArticleDTO articleDTO = new ArticleDTO();
        articleDTO.setId(article.getId());
        articleDTO.setArticleId(article.getArticleId());
        articleDTO.setTitle(article.getTitle());
        articleDTO.setContent(article.getContent());
        articleDTO.setCategory(article.getCategory());
        articleDTO.setFileName(article.getFileName());
        articleDTO.setUrlFile(article.getUrlFile());
        articleDTO.setReadingTimeMinutes(article.getReadingTimeMinutes());
        articleDTO.setDate(article.getDate());
        articleDTO.setLastUpdated(article.getLastUpdated());
        articleDTO.setSavedByUser(userService.checkIfSaved(article.getId()));
        articleDTO.setLikedByUser(likeService.isArticleLikedByUser(article.getId()));
        articleDTO.setNumberOfLikes(likeService.countLikesByArticleId(article.getId()));
        return articleDTO;
    }

    public Page<ArticleDTO> domainPageToDTOPage(Page<Article> articles, Pageable pageable) {
        return new PageImpl<>(articles.stream()
                .map(article -> {
                    try {
                        return domainToDTO(article);
                    } catch (UserNotFoundException | ArticleNotFoundException e) {
                        throw new RuntimeException("Erro ao mapear artigo com ID " + article.getId(), e);
                    }
                })
                .collect(Collectors.toList()), pageable, articles.getTotalElements());
    }

    //public Page<ArticleDTO> domainPageToDTOPage(Page<Article> articles, Pageable pageable){
    //    return new PageImpl<>(articles.stream()
    //            .map(this::domainToDTO)
    //            .collect(Collectors.toList()), pageable, articles.getTotalElements());
    //}

}
