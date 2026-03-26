package com.fabiocondo.service.impl;

import com.fabiocondo.aws.model.S3UploadResponse;
import com.fabiocondo.aws.service.AmazonS3Service;
import com.fabiocondo.domain.Topic;
import com.fabiocondo.domain.TopicContent;
import com.fabiocondo.domain.User;
import com.fabiocondo.enumeration.ContentType;
import com.fabiocondo.exception.domain.ContentNotFoundException;
import com.fabiocondo.exception.domain.DownloadRateLimitExceededException;
import com.fabiocondo.exception.domain.TopicNotFoundException;
import com.fabiocondo.repository.TopicContentRepository;
import com.fabiocondo.repository.UserRepository;
import io.github.bucket4j.Bucket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
public class TopicContentService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final String BUCKET_NAME = "cursos-bucket";

    private final TopicContentRepository contentRepository;

    private final TopicService topicService;

    private final AmazonS3Service amazonS3Service;
    public final RateLimiterService rateLimiterService;

    public final UserRepository userRepository;

    public TopicContentService(TopicContentRepository contentRepository, TopicService topicService, AmazonS3Service amazonS3Service, RateLimiterService rateLimiterService, UserRepository userRepository) {
        this.contentRepository = contentRepository;
        this.topicService = topicService;
        this.amazonS3Service = amazonS3Service;
        this.rateLimiterService = rateLimiterService;
        this.userRepository = userRepository;
    }

    public TopicContent findById(Long id) throws ContentNotFoundException {
        logger.info("Getting topic content by id: " + id);
        return contentRepository.findById(id)
                .orElseThrow(() -> new ContentNotFoundException("No content found by id: " + id));
    }

    public Page<TopicContent> findAll(Pageable pageable) {
        return contentRepository.findAll(pageable);
    }

    public TopicContent save(String description, ContentType contentType, String time, Long topicId, Integer position, MultipartFile file) throws TopicNotFoundException {
        logger.info("Uploading file: " + file.getOriginalFilename());

        String fileKey = UUID.randomUUID() + "-" + file.getOriginalFilename();
        S3UploadResponse s3UploadResponse = amazonS3Service.uploadFile(file, BUCKET_NAME, fileKey);

        Topic topic = topicService.findById(topicId);

        TopicContent content = new TopicContent();
        content.setTopic(topic);
        content.setDescription(description);
        content.setContentType(contentType);
        content.setTime(time);
        content.setPosition(position);
        content.setUrlFile(s3UploadResponse.getFileUrl());
        content.setFileName(fileKey);

        logger.info("Saving new topic content: " + content.getDescription());
        return contentRepository.save(content);
    }

    public TopicContent update(Long id, String description, ContentType contentType, String time, Long topicId, Integer position, MultipartFile file) throws ContentNotFoundException, TopicNotFoundException {

        Topic topic = topicService.findById(topicId);

        TopicContent existContent = findById(id);
        existContent.setTopic(topic);
        existContent.setDescription(description);
        existContent.setContentType(contentType);
        existContent.setTime(time);
        existContent.setPosition(position);
        // Se um novo arquivo é fornecido, atualiza o arquivo no serviço Amazon S3 e atualiza o nome e a URL do arquivo
        if (file != null) {
            if (existContent.getFileName() != null) {
                logger.info("Deleting file: " + existContent.getFileName());
                amazonS3Service.deleteFile(existContent.getFileName(), BUCKET_NAME);
            }
            String newFileKey = UUID.randomUUID() + "-" + file.getOriginalFilename();
            S3UploadResponse s3UploadResponse = amazonS3Service.uploadFile(file, BUCKET_NAME, newFileKey);
            existContent.setFileName(newFileKey);
            existContent.setUrlFile(s3UploadResponse.getFileUrl());
        }

        logger.info("Updating content: " + existContent.getDescription());
        return contentRepository.save(existContent);
    }

    public Page<TopicContent> findByTopicId(Long topicId, Pageable pageable) {
        return contentRepository.findByTopicId(topicId, pageable);
    }

    public void delete(Long id) throws ContentNotFoundException {
        TopicContent existContent = findById(id);
        logger.info("Deleting content: " + existContent.getDescription());
        contentRepository.deleteById(id);
        if (existContent.getFileName() != null) {
            logger.info("Deleting file: " + existContent.getFileName());
            amazonS3Service.deleteFile(existContent.getFileName(), BUCKET_NAME);
        }
    }

    public byte[] downloadFile(Long id, @PathVariable String fileName) throws ContentNotFoundException {
        TopicContent existContent = findById(id);
        logger.info("Downloading file: " + existContent.getFileName());
        byte[] data = amazonS3Service.downloadFile(fileName, BUCKET_NAME);
        //contentRepository.save(existContent);
        return data;
    }

    public long getTotal(){
        logger.info("Total contents: " + contentRepository.count());
        return contentRepository.count();
    }
}