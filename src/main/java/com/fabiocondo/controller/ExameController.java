package com.fabiocondo.controller;

import com.fabiocondo.domain.Exame;
import com.fabiocondo.domain.HttpResponse;
import com.fabiocondo.exception.domain.ExameNotFoundException;
import com.fabiocondo.repository.filter.ExameFilter;
import com.fabiocondo.service.ExameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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

    @GetMapping("/filter")
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
    public ResponseEntity<Exame> save(@RequestParam("institution") String institution,
                                      @RequestParam("subject") String subject,
                                      @RequestParam("description") String description,
                                      @RequestParam("level") String level,
                                      @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date date,
                                      @RequestParam("file") MultipartFile file) {

        return ResponseEntity.status(HttpStatus.OK).body(exameService.save(institution, subject, description, level, date, file));
    }

    @PutMapping
    public ResponseEntity<Exame> update(@RequestParam("id") Long id,
                                        @RequestParam("institution") String institution,
                                        @RequestParam("subject") String subject,
                                        @RequestParam("description") String description,
                                        @RequestParam("level") String level,
                                        @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date date,
                                        @RequestParam(value = "file", required = false) MultipartFile file) throws ExameNotFoundException {

        return ResponseEntity.status(HttpStatus.OK).body(exameService.update(id, institution, subject, description, level, date, file));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") Long id) throws ExameNotFoundException {
        exameService.delete(id);
        return response(HttpStatus.OK, "Exame deleted successfully");
    }

    @GetMapping("/download/{id}/{fileName}")
    public ResponseEntity<ByteArrayResource> downloadFile(@PathVariable Long id, @PathVariable String fileName) throws ExameNotFoundException {
        byte[] data = exameService.downloadFile(id, fileName);
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
        long total = exameService.getTotal();
        return ResponseEntity.status(HttpStatus.OK).body(total);
    }

    private ResponseEntity<HttpResponse> response(HttpStatus httpStatus, String message) {
        return new ResponseEntity<>(
                new HttpResponse(httpStatus.value(), httpStatus, httpStatus.getReasonPhrase().toUpperCase(), message),
                httpStatus);
    }
}
