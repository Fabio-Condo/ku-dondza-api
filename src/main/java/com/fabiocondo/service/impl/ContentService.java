package com.fabiocondo.service.impl;

import com.fabiocondo.aws.model.S3UploadResponse;
import com.fabiocondo.aws.service.AmazonS3Service;
import com.fabiocondo.domain.Module; // Importação explícita
import com.fabiocondo.domain.Content; // Importação explícita
import com.fabiocondo.enumeration.ContentType;
import com.fabiocondo.exception.domain.ContentNotFoundException;
import com.fabiocondo.exception.domain.ModuleNotFoundException;
import com.fabiocondo.repository.ContentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ContentService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final String BUCKET_NAME = "b-tests-bucket";

    private final ContentRepository contentRepository;

    private final ModuleService moduleService;

    private final AmazonS3Service amazonS3Service;

    public ContentService(ContentRepository contentRepository, ModuleService moduleService, AmazonS3Service amazonS3Service) {
        this.contentRepository = contentRepository;
        this.moduleService = moduleService;
        this.amazonS3Service = amazonS3Service;
    }

    public Content findById(Long id) throws ContentNotFoundException {
        logger.info("Getting course content by id: " + id);
        return contentRepository.findById(id)
                .orElseThrow(() -> new ContentNotFoundException("No content found by id: " + id));
    }

    public Page<Content> findAll(Pageable pageable) {
        return contentRepository.findAll(pageable);
    }

    public Content save(String description, ContentType contentType, String time, Long moduleId, Integer position, MultipartFile file) throws ModuleNotFoundException {
        logger.info("Uploading file: " + file.getOriginalFilename());
        S3UploadResponse s3UploadResponse = amazonS3Service.uploadFile(file, BUCKET_NAME);

        Module module = moduleService.findById(moduleId); // Nome totalmente qualificado

        Content content = new Content();
        content.setModule(module);
        content.setDescription(description);
        content.setContentType(contentType);
        content.setTime(time);
        content.setPosition(position);
        content.setUrlFile(s3UploadResponse.getFileUrl());
        content.setFileName(file.getOriginalFilename());

        logger.info("Saving new course content: " + content.getDescription());
        return contentRepository.save(content);
    }

    public Content update(Long id, String description, ContentType contentType, String time, Long moduleId, Integer position, MultipartFile file) throws ContentNotFoundException, ModuleNotFoundException {
        com.fabiocondo.domain.Module module = moduleService.findById(moduleId); // Nome totalmente qualificado

        Content existContent = findById(id);
        existContent.setModule(module);
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
            S3UploadResponse s3UploadResponse = amazonS3Service.uploadFile(file, BUCKET_NAME);
            existContent.setUrlFile(s3UploadResponse.getFileUrl());
            existContent.setFileName(file.getOriginalFilename());
        }

        logger.info("Updating content: " + existContent.getDescription());
        return contentRepository.save(existContent);
    }

    public Page<Content> findByModuleId(Long moduleId, Pageable pageable) {
        return contentRepository.findByModuleId(moduleId, pageable);
    }

    public void delete(Long id) throws ContentNotFoundException {
        Content existContent = findById(id);
        logger.info("Deleting content: " + existContent.getDescription());
        contentRepository.deleteById(id);
        if (existContent.getFileName() != null) {
            logger.info("Deleting file: " + existContent.getFileName());
            amazonS3Service.deleteFile(existContent.getFileName(), BUCKET_NAME);
        }
    }

    public byte[] downloadFile(Long id, @PathVariable String fileName) throws ContentNotFoundException {
        Content existContent = findById(id);
        logger.info("Downloading file: " + existContent.getFileName());
        byte[] data = amazonS3Service.downloadFile(fileName, BUCKET_NAME);
        contentRepository.save(existContent);
        return data;
    }

    public long getTotal(){
        logger.info("Total contents: " + contentRepository.count());
        return contentRepository.count();
    }
}