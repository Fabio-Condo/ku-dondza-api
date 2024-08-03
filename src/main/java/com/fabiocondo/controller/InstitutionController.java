package com.fabiocondo.controller;

import com.fabiocondo.domain.HttpResponse;
import com.fabiocondo.domain.Institution;
import com.fabiocondo.enumeration.AdministrationType;
import com.fabiocondo.enumeration.Country;
import com.fabiocondo.exception.domain.ExameNotFoundException;
import com.fabiocondo.exception.domain.InstituicaoNotFoundException;
import com.fabiocondo.repository.filter.InstitutionFilter;
import com.fabiocondo.service.impl.InstitutionServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/institutions")
public class InstitutionController {

    public InstitutionServiceImpl institutionServiceImpl;

    @Autowired
    public InstitutionController(InstitutionServiceImpl institutionServiceImpl) {
        this.institutionServiceImpl = institutionServiceImpl;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Institution> findById(@PathVariable("id") Long id) throws InstituicaoNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(institutionServiceImpl.findById(id));
    }

    @GetMapping
    public ResponseEntity<Page<Institution>> findAll(Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(institutionServiceImpl.findAll(pageable));
    }

    @GetMapping("/filter")
    public Page<Institution> filter(InstitutionFilter institutionFilter, Pageable pageable) {
        return institutionServiceImpl.filter(institutionFilter, pageable);
    }

    @PostMapping
    public ResponseEntity<Institution> save(@RequestParam("name") String name,
                                            @RequestParam("acronym") String acronym,
                                            @RequestParam("type") String type,
                                            @RequestParam("administrationType") AdministrationType administrationType,
                                            @RequestParam("country") Country country,
                                            @RequestParam("address") String address,
                                            @RequestParam("description") String description,
                                            @RequestParam("website") String website,
                                            @RequestParam("file") MultipartFile file) throws InstituicaoNotFoundException {

        return ResponseEntity.status(HttpStatus.OK).body(institutionServiceImpl.save(name, acronym, type, administrationType, country, address, description, website, file));
    }

    @PutMapping
    public ResponseEntity<Institution> update(@RequestParam("id") Long id,
                                              @RequestParam("name") String name,
                                              @RequestParam("acronym") String acronym,
                                              @RequestParam("type") String type,
                                              @RequestParam("administrationType") AdministrationType administrationType,
                                              @RequestParam("country") Country country,
                                              @RequestParam("address") String address,
                                              @RequestParam("description") String description,
                                              @RequestParam("website") String website,
                                              @RequestParam(value = "file", required = false) MultipartFile file) throws ExameNotFoundException, InstituicaoNotFoundException {

        return ResponseEntity.status(HttpStatus.OK).body(institutionServiceImpl.update(id, name, acronym, type, administrationType, country, address, description, website, file));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") Long id) throws InstituicaoNotFoundException {
        institutionServiceImpl.delete(id);
        return response(HttpStatus.OK, "Institution deleted successfully");
    }

    @GetMapping("/total")
    public ResponseEntity<Long> getTotal(){
        return ResponseEntity.status(HttpStatus.OK).body(institutionServiceImpl.getTotal());
    }

    private ResponseEntity<HttpResponse> response(HttpStatus httpStatus, String message) {
        return new ResponseEntity<>(
                new HttpResponse(httpStatus.value(), httpStatus, httpStatus.getReasonPhrase().toUpperCase(), message),
                httpStatus);
    }
}
