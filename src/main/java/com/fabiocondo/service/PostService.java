package com.fabiocondo.service;

import com.fabiocondo.domain.Post;
import com.fabiocondo.exception.domain.NotAnImageFileException;
import com.fabiocondo.exception.domain.PostNotFoundException;
import com.fabiocondo.repository.PostRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Date;

import static com.fabiocondo.constant.FileConstant.*;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.springframework.http.MediaType.*;

@Service
public class PostService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public static final String NO_POST_FOUND_BY_ID = "No post found by id: ";
    public static final String DELETING_POST = "Deleting post with id: ";

    @Autowired
    public PostRepository postRepository;

    public Post findById(Long id) throws PostNotFoundException {
        return postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException(NO_POST_FOUND_BY_ID + id));
    }

    public Page<Post> findAll(String property, Pageable pageable) {
        return postRepository.findAll(pageable);
    }

    public Post addNew(String text, MultipartFile postImage) throws IOException, NotAnImageFileException {
        logger.info("New post saved: " + text);
        Post newPost = new Post();
        newPost.setText(text);
        newPost.setDate(new Date());
        newPost.setImageUrl(getTemporaryPostImageUrl(text));
        savePostImage(newPost, postImage);
        postRepository.save(newPost);
        logger.info("New post saved: " + text);
        return newPost;
    }

    public Post update(String text, MultipartFile postImage) throws IOException, NotAnImageFileException {
        Post newPost = new Post();
        newPost.setText(text);
        newPost.setImageUrl(getTemporaryPostImageUrl(text));
        savePostImage(newPost, postImage);
        postRepository.save(newPost);
        logger.info("Post updated: " + text);
        return newPost;
    }

    public void delete(Long id) throws PostNotFoundException {
        findById(id);
        logger.info(DELETING_POST + id);
        postRepository.deleteById(id);
    }

    private void savePostImage(Post post, MultipartFile postImage) throws IOException, NotAnImageFileException {
        if (postImage != null) {
            if(!Arrays.asList(IMAGE_JPEG_VALUE, IMAGE_PNG_VALUE, IMAGE_GIF_VALUE).contains(postImage.getContentType())) {
                throw new NotAnImageFileException(postImage.getOriginalFilename() + NOT_AN_IMAGE_FILE);
            }
            Path postFolder = Paths.get(POST_FOLDER + post.getText()).toAbsolutePath().normalize();
            if(!Files.exists(postFolder)) {
                Files.createDirectories(postFolder);
                logger.info(DIRECTORY_CREATED + postFolder);
            }
            Files.deleteIfExists(Paths.get(postFolder + post.getText() + DOT + JPG_EXTENSION));
            Files.copy(postImage.getInputStream(), postFolder.resolve(post.getText() + DOT + JPG_EXTENSION), REPLACE_EXISTING);
            post.setImageUrl(setPostImageUrl(post.getText()));
            postRepository.save(post);
            logger.info(FILE_SAVED_IN_FILE_SYSTEM + postImage.getOriginalFilename());
        }
    }

    private String setPostImageUrl(String text) {
        return ServletUriComponentsBuilder.fromCurrentContextPath().path(POST_IMAGE_PATH + text + FORWARD_SLASH
                + text + DOT + JPG_EXTENSION).toUriString();
    }

    private String getTemporaryPostImageUrl(String text) {
        return ServletUriComponentsBuilder.fromCurrentContextPath().path(DEFAULT_POST_IMAGE_PATH + text).toUriString();
    }

}
