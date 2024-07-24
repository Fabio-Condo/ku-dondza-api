package com.fabiocondo.controller;

import com.fabiocondo.domain.*;
import com.fabiocondo.exception.domain.NotAnImageFileException;
import com.fabiocondo.exception.domain.PostNotFoundException;
import com.fabiocondo.exception.domain.UserNotFoundException;
import com.fabiocondo.domain.User;
import com.fabiocondo.service.impl.PostServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;

@RestController
@RequestMapping(path = {"/post"})
public class PostController{

    @Autowired
    PostServiceImpl postServiceImpl;

    @GetMapping("/{id}")
    public ResponseEntity<Post> findById(@PathVariable("id") Long id) throws PostNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(postServiceImpl.findById(id));
    }

    @GetMapping("/user/{userId}")
    public Page<Post> findByUserId(@PathVariable Long userId, Pageable pageable) {
        return postServiceImpl.findByUserId(userId, pageable);
    }

    @GetMapping("/all")
    public Page<Post> findAll(Pageable pageable) {
        return postServiceImpl.findAll(pageable);
    }

    @PostMapping("/add")
    public ResponseEntity<Post> save(@RequestParam("text") String text,
                                       @RequestParam(value = "file", required = false) MultipartFile file) throws UserNotFoundException {

        return ResponseEntity.status(HttpStatus.OK).body(postServiceImpl.save(text, file));
    }

    @PostMapping("/update")
    public ResponseEntity<Post> update(@RequestParam("id") Long id,
                                       @RequestParam("text") String text,
                                       @RequestParam(value = "file", required = false) MultipartFile file) throws IOException, NotAnImageFileException, PostNotFoundException {

        return ResponseEntity.status(HttpStatus.OK).body(postServiceImpl.update(id, text, file));
    }


    public ResponseEntity<?> delete(@PathVariable("id") Long id) throws PostNotFoundException {
        postServiceImpl.delete(id);
        return response(HttpStatus.OK, "Post deleted successfully");
    }

    private ResponseEntity<HttpResponse> response(HttpStatus httpStatus, String message) {
        return new ResponseEntity<>(
                new HttpResponse(httpStatus.value(), httpStatus, httpStatus.getReasonPhrase().toUpperCase(), message),
                httpStatus);
    }

}
