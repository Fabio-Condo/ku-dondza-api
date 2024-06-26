package com.fabiocondo.service;

import com.fabiocondo.domain.Post;
import com.fabiocondo.exception.domain.PostNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface PostService {
    Post findById(Long id) throws PostNotFoundException;

    Page<Post> findAll(Pageable pageable);

    Post save(String text, MultipartFile file);

    Post update(Long id, String text, MultipartFile file) throws PostNotFoundException;

    void delete(Long id) throws PostNotFoundException;
}
