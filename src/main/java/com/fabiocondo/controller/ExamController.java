package com.fabiocondo.controller;

import com.fabiocondo.domain.Exam;
import com.fabiocondo.domain.HttpResponse;
import com.fabiocondo.enumeration.ExamType;
import com.fabiocondo.exception.domain.ExamNotFoundException;
import com.fabiocondo.exception.domain.SubjectNotFoundException;
import com.fabiocondo.repository.filter.ExamFilter;
import com.fabiocondo.service.impl.ExamServiceImpl;
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
public class ExamController {

    public ExamServiceImpl exameServiceImpl;

    @Autowired
    public ExamController(ExamServiceImpl exameServiceImpl) {
        this.exameServiceImpl = exameServiceImpl;
    }

    @GetMapping("/filter")
    public Page<Exam> filter(ExamFilter examFilter, Pageable pageable) {
        return exameServiceImpl.filter(examFilter, pageable);
    }

    @GetMapping("/list")
    public ResponseEntity<List<Exam>> findAll() {
        return ResponseEntity.status(HttpStatus.OK).body(exameServiceImpl.findAll());
    }

    @GetMapping("/findAll")
    public ResponseEntity<Page<Exam>> findAll(Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(exameServiceImpl.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Exam> findById(@PathVariable("id") Long id) throws ExamNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(exameServiceImpl.findById(id));
    }

    @PostMapping
    public ResponseEntity<Exam> save(@RequestParam("examType") ExamType examType,
                                     @RequestParam("premium") boolean premium,
                                     @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date date,
                                     @RequestParam("subjectId") Long subjectId,
                                     @RequestParam("file") MultipartFile file) throws SubjectNotFoundException {

        return ResponseEntity.status(HttpStatus.OK).body(exameServiceImpl.save(examType, premium, date, subjectId, file));
    }

    @PutMapping
    public ResponseEntity<Exam> update(@RequestParam("id") Long id,
                                       @RequestParam("examType") ExamType examType,
                                       @RequestParam("premium") boolean premium,
                                       @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date date,
                                       @RequestParam("subjectId") Long subjectId,
                                       @RequestParam(value = "file", required = false) MultipartFile file) throws ExamNotFoundException, SubjectNotFoundException {

        return ResponseEntity.status(HttpStatus.OK).body(exameServiceImpl.update(id, examType, premium, date, subjectId, file));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") Long id) throws ExamNotFoundException {
        exameServiceImpl.delete(id);
        return response(HttpStatus.OK, "Exam deleted successfully");
    }

    @GetMapping("/download/{id}/{fileName}")
    public ResponseEntity<ByteArrayResource> downloadFile(@PathVariable Long id, @PathVariable String fileName) throws ExamNotFoundException {
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