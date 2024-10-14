package com.fabiocondo.service.impl;

import com.fabiocondo.aws.model.S3UploadResponse;
import com.fabiocondo.aws.service.AmazonS3Service;
import com.fabiocondo.domain.*;
import com.fabiocondo.exception.domain.CourseContentNotFoundException;
import com.fabiocondo.exception.domain.CourseNotFoundException;
import com.fabiocondo.repository.OnlineCourseContentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.multipart.MultipartFile;

@Service
public class OnlineCourseContentService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final String BUCKET_NAME = "b-tests-bucket";

    private final OnlineCourseContentRepository onlineCourseContentRepository;

    private final OnlineCourseService onlineCourseService;

    private final AmazonS3Service amazonS3Service;

    public OnlineCourseContentService(OnlineCourseContentRepository onlineCourseContentRepository, OnlineCourseService onlineCourseService, AmazonS3Service amazonS3Service) {
        this.onlineCourseContentRepository = onlineCourseContentRepository;
        this.onlineCourseService = onlineCourseService;
        this.amazonS3Service = amazonS3Service;
    }

    public OnlineCourseContent findById(Long id) throws CourseContentNotFoundException {
        logger.info("Getting course content by id: " + id);
        return onlineCourseContentRepository.findById(id)
                .orElseThrow(() -> new CourseContentNotFoundException("No content found by id: " + id));
    }

    public Page<OnlineCourseContent> findAll(Pageable pageable) {
        return onlineCourseContentRepository.findAll(pageable);
    }

    public OnlineCourseContent save(String description, Long onlineCourseId, MultipartFile file) throws CourseNotFoundException {
        logger.info("Uploading file: " + file.getOriginalFilename());
        S3UploadResponse s3UploadResponse = amazonS3Service.uploadFile(file, BUCKET_NAME);

        OnlineCourse course = onlineCourseService.findById(onlineCourseId);

        OnlineCourseContent content = new OnlineCourseContent();
        content.setOnlineCourse(course);
        content.setDescription(description);
        content.setUrlFile(s3UploadResponse.getFileUrl());
        content.setFileName(file.getOriginalFilename());

        logger.info("Saving new course content: " + content.getDescription());
        return onlineCourseContentRepository.save(content);
    }

    public OnlineCourseContent update(Long id, String description, Long onlineCourseId, MultipartFile file) throws CourseContentNotFoundException, CourseNotFoundException {
        OnlineCourse course = onlineCourseService.findById(onlineCourseId);

        OnlineCourseContent existContent = findById(id);
        existContent.setOnlineCourse(course);
        existContent.setDescription(description);
        // Se um novo arquivo é fornecido, atualiza o arquivo no serviço Amazon S3 e atualiza o nome e a URL do arquivo
        if (file != null) {
            if (existContent.getFileName() != null) {
                logger.info("Deleting file: " + existContent.getFileName());
                amazonS3Service.deleteFile(existContent.getFileName(), BUCKET_NAME);
            }
            S3UploadResponse s3UploadResponse = amazonS3Service.uploadFile(file, BUCKET_NAME);
            existContent.setUrlFile(s3UploadResponse.getFileUrl());
            existContent.setFileName(file.getOriginalFilename());
        }

        logger.info("Updating content: " + existContent.getDescription());
        return onlineCourseContentRepository.save(existContent);
    }

    public Page<OnlineCourseContent> findByOnlineCourseId(Long onlineCourseId, Pageable pageable) {
        return onlineCourseContentRepository.findByOnlineCourseId(onlineCourseId, pageable);
    }

    public void delete(Long id) throws CourseContentNotFoundException {
        OnlineCourseContent existContent = findById(id);
        logger.info("Deleting content: " + existContent.getDescription());
        onlineCourseContentRepository.deleteById(id);
        if (existContent.getFileName() != null) {
            logger.info("Deleting file: " + existContent.getFileName());
            amazonS3Service.deleteFile(existContent.getFileName(), BUCKET_NAME);
        }
    }

    public byte[] downloadFile(Long id, @PathVariable String fileName) throws CourseContentNotFoundException {
        OnlineCourseContent existContent = findById(id);
        logger.info("Downloading file: " + existContent.getFileName());
        byte[] data = amazonS3Service.downloadFile(fileName, BUCKET_NAME);
        onlineCourseContentRepository.save(existContent);
        return data;
    }

    public long getTotal(){
        logger.info("Total contents: " + onlineCourseContentRepository.count());
        return onlineCourseContentRepository.count();
    }
}
