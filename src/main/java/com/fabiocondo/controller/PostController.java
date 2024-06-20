package com.fabiocondo.controller;

import com.fabiocondo.domain.*;
import com.fabiocondo.exception.ExceptionHandling;
import com.fabiocondo.exception.domain.NotAnImageFileException;
import com.fabiocondo.exception.domain.PostNotFoundException;
import com.fabiocondo.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static com.fabiocondo.constant.FileConstant.*;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.IMAGE_JPEG_VALUE;

@RestController
@RequestMapping(path = {"/post"})
public class PostController{

    public static final String POST_DELETED_SUCCESSFULLY = "Post deleted successfully";
    @Autowired
    PostService postService;

    @GetMapping("/{id}")
    public ResponseEntity<Post> findById(@PathVariable("id") Long id) throws PostNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(postService.findById(id));
    }

    @GetMapping("/all")
    public Page<Post> findAll(Pageable pageable) {
        return postService.findAll(pageable);
    }

    @PostMapping("/add")
    public ResponseEntity<Post> save(@RequestParam("text") String text,
                                       @RequestParam(value = "file", required = false) MultipartFile file) {

        return ResponseEntity.status(HttpStatus.OK).body(postService.save(text, file));
    }

    @PostMapping("/update")
    public ResponseEntity<Post> update(@RequestParam("id") Long id,
                                       @RequestParam("text") String text,
                                       @RequestParam(value = "file", required = false) MultipartFile file) throws IOException, NotAnImageFileException, PostNotFoundException {

        return ResponseEntity.status(HttpStatus.OK).body(postService.update(id, text, file));
    }


    public ResponseEntity<?> delete(@PathVariable("id") Long id) throws PostNotFoundException {
        postService.delete(id);
        return response(HttpStatus.OK, POST_DELETED_SUCCESSFULLY);
    }

    @GetMapping(path = "/image/{text}/{fileName}", produces = IMAGE_JPEG_VALUE)
    public byte[] getProfileImage(@PathVariable("text") String text, @PathVariable("fileName") String fileName) throws IOException {
        return Files.readAllBytes(Paths.get(POST_FOLDER + text + FORWARD_SLASH + fileName));
    }

    private ResponseEntity<HttpResponse> response(HttpStatus httpStatus, String message) {
        return new ResponseEntity<>(
                new HttpResponse(httpStatus.value(), httpStatus, httpStatus.getReasonPhrase().toUpperCase(), message),
                httpStatus);
    }

}
