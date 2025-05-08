package com.fabiocondo.controller;

import com.fabiocondo.domain.Article;
import com.fabiocondo.domain.HttpResponse;
import com.fabiocondo.exception.domain.ArticleNotFoundException;
import com.fabiocondo.exception.domain.BookNotFoundException;
import com.fabiocondo.exception.domain.SubjectNotFoundException;
import com.fabiocondo.repository.filter.ArticleFilter;
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

    public ArticleController(ArticleServiceImpl articleService) {
        this.articleService = articleService;
    }

    @GetMapping("/filter")
    public Page<Article> filter(ArticleFilter articleFilter, Pageable pageable) {
        return articleService.filter(articleFilter, pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Article> findById(@PathVariable("id") Long id) throws ArticleNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(articleService.findById(id));
    }

    @GetMapping("/find-by-articleId/{articleId}")
    public ResponseEntity<Article> findArticleByArticleId(@PathVariable("articleId") String articleId) throws ArticleNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(articleService.findArticleByArticleId(articleId));
    }

    @PostMapping
    public ResponseEntity<Article> save(@RequestParam("title") String title,
                                        @RequestParam("content") String content,
                                        @RequestParam("file") MultipartFile file) throws SubjectNotFoundException {

        return ResponseEntity.status(HttpStatus.OK).body(articleService.save(title, content, file));
    }

    @PutMapping
    public ResponseEntity<Article> update(@RequestParam("id") Long id,
                                          @RequestParam("title") String title,
                                          @RequestParam("content") String content,
                                          @RequestParam(value = "file", required = false) MultipartFile file) throws SubjectNotFoundException, ArticleNotFoundException {

        return ResponseEntity.status(HttpStatus.OK).body(articleService.update(id, title, content, file));
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
