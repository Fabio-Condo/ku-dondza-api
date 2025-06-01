package com.fabiocondo.dtoMapper;

import com.fabiocondo.domain.Article;
import com.fabiocondo.domain.User;
import com.fabiocondo.dto.ArticleDTO;
import com.fabiocondo.exception.domain.ArticleNotFoundException;
import com.fabiocondo.exception.domain.UserNotFoundException;
import com.fabiocondo.repository.UserRepository;
import com.fabiocondo.service.UserService;
import com.fabiocondo.service.impl.LikeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ArticleMapper {

    private final LikeService likeService;
    private final UserService userService;
    private final UserRepository userRepository;


    public ArticleMapper(LikeService likeService, UserService userService, UserRepository userRepository) {
        this.likeService = likeService;
        this.userService = userService;
        this.userRepository = userRepository;
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

    public ArticleDTO domainToDTO(Article article, Long currentUserId) throws UserNotFoundException, ArticleNotFoundException {
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

        Optional<User> currentUser = userRepository.findById(currentUserId);

        if(currentUser.isPresent()){
            articleDTO.setSavedByUser(userService.checkIfSaved(article.getId(), currentUserId));
            articleDTO.setLikedByUser(likeService.isArticleLikedByUser(article.getId(), currentUserId));
        }

        articleDTO.setNumberOfLikes(likeService.countLikesByArticleId(article.getId()));
        return articleDTO;
    }

    public Page<ArticleDTO> domainPageToDTOPage(Page<Article> articles, Long currentUserId, Pageable pageable) {
        Optional<User> currentUser = userRepository.findById(currentUserId);

        return new PageImpl<>(articles.stream()
                .map(article -> {
                    try {
                        return domainToDTO(article, currentUserId);
                    } catch (UserNotFoundException | ArticleNotFoundException e) {
                        throw new RuntimeException("Erro ao mapear artigo com ID " + article.getId(), e);
                    }
                })
                .collect(Collectors.toList()), pageable, articles.getTotalElements());
    }

}
