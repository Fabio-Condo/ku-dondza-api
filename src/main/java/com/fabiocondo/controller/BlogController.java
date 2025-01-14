package com.fabiocondo.controller;

import com.fabiocondo.domain.Blog;
import com.fabiocondo.domain.HttpResponse;
import com.fabiocondo.exception.domain.BlogNotFoundException;
import com.fabiocondo.exception.domain.BookNotFoundException;
import com.fabiocondo.exception.domain.SubjectNotFoundException;
import com.fabiocondo.repository.filter.BlogFilter;
import com.fabiocondo.service.impl.BlogServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/blog")
public class BlogController {

    public BlogServiceImpl blogService;

    @Autowired
    public BlogController(BlogServiceImpl blogService) {
        this.blogService = blogService;
    }

    @GetMapping("/filter")
    public Page<Blog> filter(BlogFilter blogFilter, Pageable pageable) {
        return blogService.filter(blogFilter, pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Blog> findById(@PathVariable("id") Long id) throws BlogNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(blogService.findById(id));
    }

    @GetMapping("/find-by-blogId/{blogId}")
    public ResponseEntity<Blog> findBlogByBlogId(@PathVariable("blogId") String blogId) throws BlogNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(blogService.findBlogByBlogId(blogId));
    }

    @PostMapping
    public ResponseEntity<Blog> save(@RequestParam("title") String title,
                                     @RequestParam("content") String content,
                                     @RequestParam("subjectId") Long subjectId,
                                     @RequestParam("file") MultipartFile file) throws SubjectNotFoundException {

        return ResponseEntity.status(HttpStatus.OK).body(blogService.save(title, content, subjectId, file));
    }

    @PutMapping
    public ResponseEntity<Blog> update(@RequestParam("id") Long id,
                                       @RequestParam("title") String title,
                                       @RequestParam("content") String content,
                                       @RequestParam("subjectId") Long subjectId,
                                       @RequestParam(value = "file", required = false) MultipartFile file) throws SubjectNotFoundException, BlogNotFoundException {

        return ResponseEntity.status(HttpStatus.OK).body(blogService.update(id, title, content, subjectId, file));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") Long id) throws BookNotFoundException, BlogNotFoundException {
        blogService.delete(id);
        return response(HttpStatus.OK, "Blog deleted successfully");
    }

    @GetMapping("/total")
    public ResponseEntity<Long> getTotal(){
        return ResponseEntity.status(HttpStatus.OK).body(blogService.getTotal());
    }

    private ResponseEntity<HttpResponse> response(HttpStatus httpStatus, String message) {
        return new ResponseEntity<>(
                new HttpResponse(httpStatus.value(), httpStatus, httpStatus.getReasonPhrase().toUpperCase(), message),
                httpStatus);
    }
}
