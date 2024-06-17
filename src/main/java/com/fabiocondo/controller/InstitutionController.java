package com.fabiocondo.controller;

import com.fabiocondo.domain.HttpResponse;
import com.fabiocondo.domain.Institution;
import com.fabiocondo.exception.domain.InstituicaoNotFoundException;
import com.fabiocondo.repository.filter.InstitutionFilter;
import com.fabiocondo.service.InstitutionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/institutions")
public class InstitutionController {

    public InstitutionService institutionService;

    @Autowired
    public InstitutionController(InstitutionService institutionService) {
        this.institutionService = institutionService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Institution> findById(@PathVariable("id") Long id) throws InstituicaoNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(institutionService.findById(id));
    }

    @GetMapping
    public ResponseEntity<Page<Institution>> findAll(Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(institutionService.findAll(pageable));
    }

    @GetMapping("/filter")
    public Page<Institution> filter(InstitutionFilter institutionFilter, Pageable pageable) {
        return institutionService.filter(institutionFilter, pageable);
    }

    @PostMapping
    public ResponseEntity<Institution> save(@RequestBody Institution institution) {
        return ResponseEntity.status(HttpStatus.CREATED).body(institutionService.save(institution));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Institution> update(@PathVariable("id") Long id, @RequestBody Institution institution) throws InstituicaoNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(institutionService.update(institution, id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") Long id) throws InstituicaoNotFoundException {
        institutionService.delete(id);
        return response(HttpStatus.OK, "Institution deleted successfully");
    }

    @GetMapping("/total")
    public ResponseEntity<Long> getTotal(){
        return ResponseEntity.status(HttpStatus.OK).body(institutionService.getTotal());
    }

    private ResponseEntity<HttpResponse> response(HttpStatus httpStatus, String message) {
        return new ResponseEntity<>(
                new HttpResponse(httpStatus.value(), httpStatus, httpStatus.getReasonPhrase().toUpperCase(), message),
                httpStatus);
    }
}
