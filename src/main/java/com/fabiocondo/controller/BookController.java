package com.fabiocondo.controller;

import com.fabiocondo.domain.Book;
import com.fabiocondo.domain.HttpResponse;
import com.fabiocondo.exception.domain.BookNotFoundException;
import com.fabiocondo.exception.domain.SubjectNotFoundException;
import com.fabiocondo.repository.filter.BookFilter;
import com.fabiocondo.service.impl.BookServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/books")
public class BookController {

    public BookServiceImpl bookService;

    @Autowired
    public BookController(BookServiceImpl bookService) {
        this.bookService = bookService;
    }

    @GetMapping("/filter")
    public Page<Book> filter(BookFilter bookFilter, Pageable pageable) {
        return bookService.filter(bookFilter, pageable);
    }

    @GetMapping("/list")
    public ResponseEntity<List<Book>> findAll() {
        return ResponseEntity.status(HttpStatus.OK).body(bookService.findAll());
    }

    @GetMapping("/findAll")
    public ResponseEntity<Page<Book>> findAll(Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(bookService.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Book> findById(@PathVariable("id") Long id) throws BookNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(bookService.findById(id));
    }

    @PostMapping
    public ResponseEntity<Book> save(@RequestParam("name") String name,
                                     @RequestParam("description") String description,
                                     @RequestParam("subjectId") Long subjectId,
                                     @RequestParam("file") MultipartFile file) throws SubjectNotFoundException {

        return ResponseEntity.status(HttpStatus.OK).body(bookService.save(name, description, subjectId, file));
    }

    @PutMapping
    public ResponseEntity<Book> update(@RequestParam("id") Long id,
                                       @RequestParam("name") String name,
                                       @RequestParam("description") String description,
                                       @RequestParam("subjectId") Long subjectId,
                                       @RequestParam(value = "file", required = false) MultipartFile file) throws SubjectNotFoundException, BookNotFoundException {

        return ResponseEntity.status(HttpStatus.OK).body(bookService.update(id, name, description, subjectId, file));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") Long id) throws BookNotFoundException {
        bookService.delete(id);
        return response(HttpStatus.OK, "Book deleted successfully");
    }

    @GetMapping("/download/{id}/{fileName}")
    public ResponseEntity<ByteArrayResource> downloadFile(@PathVariable Long id, @PathVariable String fileName) throws BookNotFoundException {
        byte[] data = bookService.downloadFile(id, fileName);
        ByteArrayResource resource = new ByteArrayResource(data);

        return ResponseEntity
                .status(HttpStatus.OK)
                .contentLength(data.length)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(resource);
    }

    @GetMapping("/total")
    public ResponseEntity<Long> getTotal(){
        return ResponseEntity.status(HttpStatus.OK).body(bookService.getTotal());
    }

    private ResponseEntity<HttpResponse> response(HttpStatus httpStatus, String message) {
        return new ResponseEntity<>(
                new HttpResponse(httpStatus.value(), httpStatus, httpStatus.getReasonPhrase().toUpperCase(), message),
                httpStatus);
    }
}