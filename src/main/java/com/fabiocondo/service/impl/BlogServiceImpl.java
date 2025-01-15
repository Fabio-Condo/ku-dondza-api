package com.fabiocondo.service.impl;

import com.fabiocondo.aws.model.S3UploadResponse;
import com.fabiocondo.aws.service.AmazonS3Service;
import com.fabiocondo.domain.Blog;
import com.fabiocondo.domain.Subject;
import com.fabiocondo.exception.domain.BlogNotFoundException;
import com.fabiocondo.exception.domain.SubjectNotFoundException;
import com.fabiocondo.repository.BlogRepository;
import com.fabiocondo.repository.filter.BlogFilter;
import com.fabiocondo.service.BlogService;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;

@Service
public class BlogServiceImpl implements BlogService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final String BUCKET_NAME = "b-tests-bucket";

    public BlogRepository blogRepository;

    private final AmazonS3Service amazonS3Service;

    private final SubjectServiceImpl subjectServiceImpl;

    @Autowired
    public BlogServiceImpl(BlogRepository blogRepository, AmazonS3Service amazonS3Service, SubjectServiceImpl subjectServiceImpl) {
        this.blogRepository = blogRepository;
        this.amazonS3Service = amazonS3Service;
        this.subjectServiceImpl = subjectServiceImpl;
    }

    @Override
    public Blog findById(Long id) throws BlogNotFoundException {
        logger.info("Getting blog by id: " + id);
        return blogRepository.findById(id)
                .orElseThrow(() -> new BlogNotFoundException("No blog found by id: " + id));
    }

    @Override
    public Blog findBlogByBlogId(String blogId) throws BlogNotFoundException {

        return blogRepository.findBlogByBlogId(blogId)
                .orElseThrow(() -> new BlogNotFoundException("No blog found by id: " + blogId));
    }

    @Override
    public Page<Blog> filter(BlogFilter blogFilter, Pageable pageable) {
        return blogRepository.filter(blogFilter, pageable);
    }

    @Override
    public Blog save(String title, String content, Long subjectId, MultipartFile file) throws SubjectNotFoundException {
        logger.info("Uploading file: " + file.getOriginalFilename());
        S3UploadResponse s3UploadResponse = amazonS3Service.uploadFile(file, BUCKET_NAME);

        Subject subject = subjectServiceImpl.findById(subjectId);

        Blog blog = new Blog();
        blog.setBlogId(generateBlogId());
        blog.setSubject(subject);
        blog.setTitle(title);
        blog.setContent(content);
        blog.setPostDate(new Date());
        blog.setUrlFile(s3UploadResponse.getFileUrl());
        blog.setFileName(file.getOriginalFilename());

        logger.info("Saving new blog: " + blog.getTitle());
        return blogRepository.save(blog);
    }

    @Override
    public Blog update(Long id, String title, String content, Long subjectId, MultipartFile file) throws BlogNotFoundException, SubjectNotFoundException {
        Subject subject = subjectServiceImpl.findById(subjectId);

        Blog existBlog = findById(id);
        existBlog.setSubject(subject);
        existBlog.setTitle(title);
        existBlog.setContent(content);

        // Se um novo arquivo é fornecido, atualiza o arquivo no serviço Amazon S3 e atualiza o nome e a URL do arquivo
        if (file != null) {
            if (existBlog.getFileName() != null) {
                logger.info("Deleting file: " + existBlog.getFileName());
                amazonS3Service.deleteFile(existBlog.getFileName(), BUCKET_NAME);
            }
            S3UploadResponse s3UploadResponse = amazonS3Service.uploadFile(file, BUCKET_NAME);
            existBlog.setUrlFile(s3UploadResponse.getFileUrl());
            existBlog.setFileName(file.getOriginalFilename());
        }

        logger.info("Saving new blog: " + existBlog.getTitle());
        return blogRepository.save(existBlog);
    }

    @Override
    public void delete(Long id) throws BlogNotFoundException {
        Blog existBlog = findById(id);
        logger.info("Deleting blog: " + existBlog.getTitle());
        blogRepository.deleteById(id);
        if (existBlog.getFileName() != null) {
            logger.info("Deleting file: " + existBlog.getFileName());
            amazonS3Service.deleteFile(existBlog.getFileName(), BUCKET_NAME);
        }
    }

    @Override
    public long getTotal(){
        logger.info("Total blog: " + blogRepository.count());
        return blogRepository.count();
    }

    private String generateBlogId() {
        return RandomStringUtils.randomAlphanumeric(10);
    }

}
