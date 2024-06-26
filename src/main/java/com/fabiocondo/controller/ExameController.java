package com.fabiocondo.controller;

import com.fabiocondo.domain.Exame;
import com.fabiocondo.domain.HttpResponse;
import com.fabiocondo.exception.domain.ExameNotFoundException;
import com.fabiocondo.exception.domain.InstituicaoNotFoundException;
import com.fabiocondo.exception.domain.SubjectNotFoundException;
import com.fabiocondo.repository.filter.ExameFilter;
import com.fabiocondo.service.impl.ExameServiceImpl;
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

    public ExameServiceImpl exameServiceImpl;

    @Autowired
    public ExameController(ExameServiceImpl exameServiceImpl) {
        this.exameServiceImpl = exameServiceImpl;
    }

    @GetMapping("/filter")
    public Page<Exame> filter(ExameFilter exameFilter, Pageable pageable) {
        return exameServiceImpl.filter(exameFilter, pageable);
    }

    @GetMapping("/list")
    public ResponseEntity<List<Exame>> findAll() {
        return ResponseEntity.status(HttpStatus.OK).body(exameServiceImpl.findAll());
    }

    @GetMapping("/findAll")
    public ResponseEntity<Page<Exame>> findAll(Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(exameServiceImpl.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Exame> findById(@PathVariable("id") Long id) throws ExameNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(exameServiceImpl.findById(id));
    }

    @PostMapping
    public ResponseEntity<Exame> save(@RequestParam("description") String description,
                                      @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date date,
                                      @RequestParam("subjectId") Long subjectId,
                                      @RequestParam("institutionId") Long institutionId,
                                      @RequestParam("file") MultipartFile file) throws InstituicaoNotFoundException, SubjectNotFoundException {

        return ResponseEntity.status(HttpStatus.OK).body(exameServiceImpl.save(description, date, subjectId, institutionId, file));
    }

    @PutMapping
    public ResponseEntity<Exame> update(@RequestParam("id") Long id,
                                        @RequestParam("description") String description,
                                        @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date date,
                                        @RequestParam("subjectId") Long subjectId,
                                        @RequestParam("institutionId") Long institutionId,
                                        @RequestParam(value = "file", required = false) MultipartFile file) throws ExameNotFoundException, InstituicaoNotFoundException, SubjectNotFoundException {

        return ResponseEntity.status(HttpStatus.OK).body(exameServiceImpl.update(id, description, date, subjectId, institutionId, file));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") Long id) throws ExameNotFoundException {
        exameServiceImpl.delete(id);
        return response(HttpStatus.OK, "Exame deleted successfully");
    }

    @GetMapping("/download/{id}/{fileName}")
    public ResponseEntity<ByteArrayResource> downloadFile(@PathVariable Long id, @PathVariable String fileName) throws ExameNotFoundException {
        byte[] data = exameServiceImpl.downloadFile(id, fileName);
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
        return ResponseEntity.status(HttpStatus.OK).body(exameServiceImpl.getTotal());
    }

    private ResponseEntity<HttpResponse> response(HttpStatus httpStatus, String message) {
        return new ResponseEntity<>(
                new HttpResponse(httpStatus.value(), httpStatus, httpStatus.getReasonPhrase().toUpperCase(), message),
                httpStatus);
    }
}
