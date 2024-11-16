package com.fabiocondo.service.impl;

import com.fabiocondo.aws.model.S3UploadResponse;
import com.fabiocondo.aws.service.AmazonS3Service;
import com.fabiocondo.domain.Group;
import com.fabiocondo.domain.Post;
import com.fabiocondo.domain.PostOption;
import com.fabiocondo.exception.domain.PostNotFoundException;
import com.fabiocondo.exception.domain.UserNotFoundException;
import com.fabiocondo.repository.GroupRepository;
import com.fabiocondo.repository.PostRepository;
import com.fabiocondo.service.PostService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;

@Service
public class PostServiceImpl implements PostService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final String BUCKET_NAME = "b-tests-bucket";

    private final AmazonS3Service amazonS3Service;

    @Autowired
    public PostRepository postRepository;

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private GroupRepository groupRepository;

    public PostServiceImpl(AmazonS3Service amazonS3Service, PostRepository postRepository, UserServiceImpl userService, GroupRepository groupRepository) {
        this.amazonS3Service = amazonS3Service;
        this.postRepository = postRepository;
        this.userService = userService;
        this.groupRepository = groupRepository;
    }

    @Override
    public Post findById(Long id) throws PostNotFoundException {
        return postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException("No post found by id: " + id));
    }

    @Override
    public Page<Post> findByUserId(Long userId, Pageable pageable) {
        return postRepository.findByUserId(userId, pageable);
    }

    @Override
    public Page<Post> findAll(Pageable pageable) {
        return postRepository.findAll(pageable);
    }

    @Override
    public Post save(String text, MultipartFile file) throws UserNotFoundException {
        logger.info("Uploading file: " + file.getOriginalFilename());
        S3UploadResponse s3UploadResponse = amazonS3Service.uploadFile(file, BUCKET_NAME);

        Post post = new Post();
        post.setText(text);
        post.setUrlFile(s3UploadResponse.getFileUrl());
        post.setFileName(file.getOriginalFilename());
        post.setDate(new Date());
        post.setUser(userService.getAuthenticatedUser());
        post.setGroup(null); // Se adicionar a partir do feed principal, o group deve ser null

        logger.info("Saving new post: " + post.getText());
        return postRepository.save(post);
    }

    public Post save_quiz_post(Post post) throws UserNotFoundException {
        post.setDate(new Date());
        post.setUser(userService.getAuthenticatedUser());
        post.setGroup(null); // Se adicionar a partir do feed principal, o group deve ser null
        post.getPostOptions().forEach(option -> option.setPost(post)); // if is quiz post type
        logger.info("Saving new post: " + post.getText());
        return postRepository.save(post);
    }

    @Override
    public Post update(Long id, String text, MultipartFile file) throws PostNotFoundException {
        Post existPost = findById(id);
        existPost.setText(text);

        // Se um novo arquivo é fornecido, atualiza o arquivo no serviço Amazon S3 e atualiza o nome e a URL do arquivo
        if (file != null) {
            if (existPost.getFileName() != null) {
                logger.info("Deleting file: " + existPost.getFileName());
                amazonS3Service.deleteFile(existPost.getFileName(), BUCKET_NAME);
            }
            S3UploadResponse s3UploadResponse = amazonS3Service.uploadFile(file, BUCKET_NAME);
            existPost.setUrlFile(s3UploadResponse.getFileUrl());
            existPost.setFileName(file.getOriginalFilename());
        }

        logger.info("Saving new post: " + existPost.getText());
        return postRepository.save(existPost);
    }

    @Override
    public void delete(Long id) throws PostNotFoundException {
        Post existPost = findById(id);
        logger.info("Deleting post: " + existPost.getText());
        postRepository.deleteById(id);
        if (existPost.getFileName() != null) {
            logger.info("Deleting file: " + existPost.getFileName());
            amazonS3Service.deleteFile(existPost.getFileName(), BUCKET_NAME);
        }
    }

    @Override
    public Post saveFromGroup(Long groupId, String text, MultipartFile file) throws UserNotFoundException, PostNotFoundException {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new PostNotFoundException("No Group found by id: " + groupId));

        logger.info("Uploading file: " + file.getOriginalFilename());
        S3UploadResponse s3UploadResponse = amazonS3Service.uploadFile(file, BUCKET_NAME);

        Post post = new Post();
        post.setText(text);
        post.setUrlFile(s3UploadResponse.getFileUrl());
        post.setFileName(file.getOriginalFilename());
        post.setDate(new Date());
        post.setUser(userService.getAuthenticatedUser());
        post.setGroup(group);

        logger.info("Saving new post: " + post.getText());
        return postRepository.save(post);
    }

    public Page<Post> findByGroupId(@RequestParam Long groupId, Pageable pageable) {
        return postRepository.findByGroupId(groupId, pageable);
    }

}
