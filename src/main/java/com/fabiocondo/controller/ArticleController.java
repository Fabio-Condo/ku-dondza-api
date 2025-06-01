package com.fabiocondo.controller;

import com.fabiocondo.domain.Article;
import com.fabiocondo.domain.HttpResponse;
import com.fabiocondo.dto.ArticleDTO;
import com.fabiocondo.dto.QuizDTO;
import com.fabiocondo.dtoMapper.ArticleMapper;
import com.fabiocondo.enumeration.CategoryType;
import com.fabiocondo.exception.domain.*;
import com.fabiocondo.repository.filter.ArticleFilter;
import com.fabiocondo.repository.filter.QuizFilter;
import com.fabiocondo.service.impl.ArticleServiceImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/articles")
public class ArticleController {

    public ArticleServiceImpl articleService;
    private final ArticleMapper articleMapper;

    public ArticleController(ArticleServiceImpl articleService, ArticleMapper articleMapper) {
        this.articleService = articleService;
        this.articleMapper = articleMapper;
    }

    @GetMapping("/filter")
    public Page<ArticleDTO> filter(ArticleFilter articleFilter, @RequestParam("currentUserId") Long currentUserId, Pageable pageable) {
        return articleMapper.domainPageToDTOPage(articleService.filter(articleFilter, pageable), currentUserId, pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Article> findById(@PathVariable("id") Long id) throws ArticleNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(articleService.findById(id));
    }

    @GetMapping("/find-by-articleId/{articleId}")
    public ResponseEntity<ArticleDTO> findArticleByArticleId(@PathVariable("articleId") String articleId, @RequestParam("currentUserId") Long currentUserId) throws ArticleNotFoundException, UserNotFoundException {
        Article article = articleService.findArticleByArticleId(articleId);
        return ResponseEntity.status(HttpStatus.OK).body(articleMapper.domainToDTO(article, currentUserId));
    }

    @PostMapping
    public ResponseEntity<Article> save(@RequestParam("title") String title,
                                        @RequestParam("content") String content,
                                        @RequestParam("category") CategoryType category,
                                        @RequestParam("readingTimeMinutes") int readingTimeMinutes,
                                        @RequestParam("file") MultipartFile file) throws SubjectNotFoundException {

        return ResponseEntity.status(HttpStatus.OK).body(articleService.save(title, content, category, readingTimeMinutes, file));
    }

    @PutMapping
    public ResponseEntity<Article> update(@RequestParam("id") Long id,
                                          @RequestParam("title") String title,
                                          @RequestParam("content") String content,
                                          @RequestParam("category") CategoryType category,
                                          @RequestParam("readingTimeMinutes") int readingTimeMinutes,
                                          @RequestParam(value = "file", required = false) MultipartFile file) throws SubjectNotFoundException, ArticleNotFoundException {

        return ResponseEntity.status(HttpStatus.OK).body(articleService.update(id, title, content, category, readingTimeMinutes, file));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") Long id) throws BookNotFoundException, ArticleNotFoundException {
        articleService.delete(id);
        return response(HttpStatus.OK, "Article deleted successfully");
    }

    @GetMapping("/total")
    public ResponseEntity<Long> getTotal(){
        return ResponseEntity.status(HttpStatus.OK).body(articleService.getTotal());
    }

    private ResponseEntity<HttpResponse> response(HttpStatus httpStatus, String message) {
        return new ResponseEntity<>(
                new HttpResponse(httpStatus.value(), httpStatus, httpStatus.getReasonPhrase().toUpperCase(), message),
                httpStatus);
    }
}
