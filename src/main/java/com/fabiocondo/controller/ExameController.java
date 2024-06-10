package com.fabiocondo.controller;

import com.fabiocondo.domain.Exame;
import com.fabiocondo.domain.HttpResponse;
import com.fabiocondo.exception.domain.ExameNotFoundException;
import com.fabiocondo.repository.filter.ExameFilter;
import com.fabiocondo.service.ExameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/exames")
public class ExameController {

    public ExameService exameService;

    @Autowired
    public ExameController(ExameService exameService) {
        this.exameService = exameService;
    }

    @GetMapping
    public Page<Exame> filter(ExameFilter exameFilter, Pageable pageable) {
        return exameService.filter(exameFilter, pageable);
    }

    @GetMapping("/list")
    public ResponseEntity<List<Exame>> findAll() {
        return ResponseEntity.status(HttpStatus.OK).body(exameService.findAll());
    }

    @GetMapping("/findAll")
    public ResponseEntity<Page<Exame>> findAll(Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(exameService.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Exame> findById(@PathVariable("id") Long id) throws ExameNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(exameService.findById(id));
    }

    @PostMapping
    public ResponseEntity<Exame> save(@RequestParam("subject") String subject,
                                      @RequestParam("description") String description,
                                      @RequestParam("level") String level,
                                      @RequestParam("file") MultipartFile file) {

        return ResponseEntity.status(HttpStatus.OK).body(exameService.save(subject, description, level, file));
    }

    @PutMapping
    public ResponseEntity<Exame> update(@RequestParam("id") Long id,
                                        @RequestParam("subject") String subject,
                                        @RequestParam("description") String description,
                                        @RequestParam("level") String level,
                                        @RequestParam("file") MultipartFile file) throws ExameNotFoundException {

        return ResponseEntity.status(HttpStatus.OK).body(exameService.update(id, subject, description, level, file));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") Long id) throws ExameNotFoundException {
        exameService.delete(id);
        return response(HttpStatus.OK, "Exame deleted successfully");
    }

    @GetMapping("/total")
    public ResponseEntity<Long> getTotal(){
        long total = exameService.getTotal();
        return ResponseEntity.status(HttpStatus.OK).body(total);
    }

    private ResponseEntity<HttpResponse> response(HttpStatus httpStatus, String message) {
        return new ResponseEntity<>(
                new HttpResponse(httpStatus.value(), httpStatus, httpStatus.getReasonPhrase().toUpperCase(), message),
                httpStatus);
    }
}
