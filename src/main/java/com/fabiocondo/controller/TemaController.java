package com.fabiocondo.controller;

import com.fabiocondo.domain.HttpResponse;
import com.fabiocondo.domain.Tema;
import com.fabiocondo.exception.domain.CourseContentNotFoundException;
import com.fabiocondo.exception.domain.CourseNotFoundException;
import com.fabiocondo.exception.domain.TemaNotFoundException;
import com.fabiocondo.service.impl.TemaService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/temas")
public class TemaController {

    private final TemaService temaService;

    public TemaController(TemaService temaService) {
        this.temaService = temaService;
    }


    @GetMapping("/{id}")
    public ResponseEntity<Tema> findById(@PathVariable("id") Long id) throws TemaNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(temaService.findById(id));
    }

    @GetMapping("/findByCourseId")
    public ResponseEntity<Page<Tema>> findByOnlineCourseId(@RequestParam Long courseId, Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(temaService.findByOnlineCourseId(courseId, pageable));
    }

    @GetMapping("/getListByCourseId")
    public ResponseEntity<List<Tema>> findByOnlineCourseId(@RequestParam Long courseId) {
        return ResponseEntity.status(HttpStatus.OK).body(temaService.findByOnlineCourseId(courseId));
    }

    @GetMapping
    public ResponseEntity<Page<Tema>> findAll(@RequestParam(required = false, defaultValue = "") String searchParam, Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(temaService.findAll(searchParam, pageable));
    }

    @GetMapping("/findAll")
    public ResponseEntity<List<Tema>> findAll() {
        List<Tema> temas = temaService.findAll();
        return ResponseEntity.ok(temas);
    }

    @PostMapping
    public ResponseEntity<Tema> save(@RequestParam("name") String name,
                                                    @RequestParam("onlineCourseId") Long onlineCourseId,
                                                    @RequestParam("file") MultipartFile file) throws CourseNotFoundException, TemaNotFoundException {

        return ResponseEntity.status(HttpStatus.OK).body(temaService.save(name, onlineCourseId, file));
    }

    @PutMapping
    public ResponseEntity<Tema> update(@RequestParam("id") Long id,
                                                      @RequestParam("name") String name,
                                                      @RequestParam("onlineCourseId") Long onlineCourseId,
                                                      @RequestParam(value = "file", required = false) MultipartFile file) throws CourseContentNotFoundException, CourseNotFoundException, TemaNotFoundException {

        return ResponseEntity.status(HttpStatus.OK).body(temaService.update(id, name, onlineCourseId, file));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") Long id) throws TemaNotFoundException {
        temaService.delete(id);
        return response(HttpStatus.OK, "Tema deleted successfully");
    }

    @GetMapping("/total")
    public ResponseEntity<Long> getTotal(){
        return ResponseEntity.status(HttpStatus.OK).body(temaService.getTotal());
    }

    private ResponseEntity<HttpResponse> response(HttpStatus httpStatus, String message) {
        return new ResponseEntity<>(
                new HttpResponse(httpStatus.value(), httpStatus, httpStatus.getReasonPhrase().toUpperCase(), message),
                httpStatus);
    }
}
