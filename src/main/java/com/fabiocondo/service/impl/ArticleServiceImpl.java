package com.fabiocondo.service.impl;

import com.fabiocondo.aws.model.S3UploadResponse;
import com.fabiocondo.aws.service.AmazonS3Service;
import com.fabiocondo.domain.Article;
import com.fabiocondo.domain.Like;
import com.fabiocondo.domain.User;
import com.fabiocondo.enumeration.CategoryType;
import com.fabiocondo.exception.domain.ArticleNotFoundException;
import com.fabiocondo.exception.domain.SubjectNotFoundException;
import com.fabiocondo.exception.domain.UserNotFoundException;
import com.fabiocondo.repository.ArticleRepository;
import com.fabiocondo.repository.filter.ArticleFilter;
import com.fabiocondo.service.ArticleService;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.Optional;

@Service
public class ArticleServiceImpl implements ArticleService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final String BUCKET_NAME = "b-tests-bucket";

    public ArticleRepository articleRepository;

    private final AmazonS3Service amazonS3Service;

    private final UserServiceImpl userService;

    @Autowired
    public ArticleServiceImpl(ArticleRepository articleRepository, AmazonS3Service amazonS3Service, UserServiceImpl userService) {
        this.articleRepository = articleRepository;
        this.amazonS3Service = amazonS3Service;
        this.userService = userService;
    }

    @Override
    public Article findById(Long id) throws ArticleNotFoundException {
        logger.info("Getting article by id: " + id);
        return articleRepository.findById(id)
                .orElseThrow(() -> new ArticleNotFoundException("No article found by id: " + id));
    }

    @Override
    public Article findArticleByArticleId(String articleId) throws ArticleNotFoundException {
        return articleRepository.findArticleByArticleId(articleId)
                .orElseThrow(() -> new ArticleNotFoundException("No article found by id: " + articleId));
    }

    @Override
    public Page<Article> filter(ArticleFilter articleFilter, Pageable pageable) {
        return articleRepository.filter(articleFilter, pageable);
    }

    @Override
    public Article save(String title, String content, CategoryType category, int readingTimeMinutes, MultipartFile file) throws SubjectNotFoundException {
        logger.info("Uploading file: " + file.getOriginalFilename());
        //S3UploadResponse s3UploadResponse = amazonS3Service.uploadFile(file, BUCKET_NAME);

        Article article = new Article();
        article.setArticleId(generateArticleIdId());
        article.setTitle(title);
        article.setContent(content);
        article.setCategory(category);
        article.setReadingTimeMinutes(readingTimeMinutes);
        article.setDate(new Date());
        //article.setUrlFile(s3UploadResponse.getFileUrl());
        article.setFileName(file.getOriginalFilename());

        logger.info("Saving new article: " + article.getTitle());
        return articleRepository.save(article);
    }

    @Override
    public Article update(Long id, String title, String content, CategoryType category, int readingTimeMinutes, MultipartFile file) throws ArticleNotFoundException, SubjectNotFoundException {
        Article existArticle = findById(id);
        existArticle.setTitle(title);
        existArticle.setContent(content);
        existArticle.setCategory(category);
        existArticle.setReadingTimeMinutes(readingTimeMinutes);
        existArticle.setLastUpdated(new Date());

        // Se um novo arquivo é fornecido, atualiza o arquivo no serviço Amazon S3 e atualiza o nome e a URL do arquivo
        if (file != null) {
            if (existArticle.getFileName() != null) {
                logger.info("Deleting file: " + existArticle.getFileName());
                amazonS3Service.deleteFile(existArticle.getFileName(), BUCKET_NAME);
            }
            S3UploadResponse s3UploadResponse = amazonS3Service.uploadFile(file, BUCKET_NAME);
            existArticle.setUrlFile(s3UploadResponse.getFileUrl());
            existArticle.setFileName(file.getOriginalFilename());
        }

        logger.info("Saving new article: " + existArticle.getTitle());
        return articleRepository.save(existArticle);
    }

    @Override
    public void delete(Long id) throws ArticleNotFoundException {
        Article existArticle = findById(id);
        logger.info("Deleting article: " + existArticle.getTitle());
        articleRepository.deleteById(id);
        if (existArticle.getFileName() != null) {
            logger.info("Deleting file: " + existArticle.getFileName());
            amazonS3Service.deleteFile(existArticle.getFileName(), BUCKET_NAME);
        }
    }

    @Override
    public long getTotal(){
        logger.info("Total article: " + articleRepository.count());
        return articleRepository.count();
    }

    private String generateArticleIdId() {
        return RandomStringUtils.randomAlphanumeric(10);
    }

}
