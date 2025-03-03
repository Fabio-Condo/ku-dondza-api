package com.fabiocondo.controller;

import com.fabiocondo.domain.HttpResponse;
import com.fabiocondo.domain.Module;
import com.fabiocondo.exception.domain.CourseContentNotFoundException;
import com.fabiocondo.exception.domain.ModuleNotFoundException;
import com.fabiocondo.exception.domain.OnlineCourseNotFoundException;
import com.fabiocondo.service.impl.ModuleService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/modules")
public class ModuleController {

    private final ModuleService moduleService;

    public ModuleController(ModuleService moduleService) {
        this.moduleService = moduleService;
    }


    @GetMapping("/{id}")
    public ResponseEntity<Module> findById(@PathVariable("id") Long id) throws ModuleNotFoundException {
        return ResponseEntity.status(HttpStatus.OK).body(moduleService.findById(id));
    }

    @GetMapping("/findByCourseId")
    public ResponseEntity<List<Module>> findByOnlineCourseId(@RequestParam Long courseId) {
        return ResponseEntity.status(HttpStatus.OK).body(moduleService.findByOnlineCourseId(courseId));
    }

    @GetMapping
    public ResponseEntity<Page<Module>> findAll(@RequestParam(required = false, defaultValue = "") String searchParam, Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(moduleService.findAll(searchParam, pageable));
    }

    @GetMapping("/findAll")
    public ResponseEntity<List<Module>> findAll() {
        List<Module> modules = moduleService.findAll();
        return ResponseEntity.ok(modules);
    }

    @PostMapping
    public ResponseEntity<Module> save(@RequestParam("name") String name,
                                       @RequestParam("onlineCourseId") Long onlineCourseId,
                                       @RequestParam("position") Integer position) throws ModuleNotFoundException, OnlineCourseNotFoundException {

        return ResponseEntity.status(HttpStatus.OK).body(moduleService.save(name, onlineCourseId, position));
    }

    @PutMapping
    public ResponseEntity<Module> update(@RequestParam("id") Long id,
                                         @RequestParam("name") String name,
                                         @RequestParam("onlineCourseId") Long onlineCourseId,
                                         @RequestParam("position") Integer position) throws CourseContentNotFoundException, ModuleNotFoundException, OnlineCourseNotFoundException {

        return ResponseEntity.status(HttpStatus.OK).body(moduleService.update(id, name, onlineCourseId, position));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") Long id) throws ModuleNotFoundException {
        moduleService.delete(id);
        return response(HttpStatus.OK, "Module deleted successfully");
    }

    @GetMapping("/total")
    public ResponseEntity<Long> getTotal(){
        return ResponseEntity.status(HttpStatus.OK).body(moduleService.getTotal());
    }

    private ResponseEntity<HttpResponse> response(HttpStatus httpStatus, String message) {
        return new ResponseEntity<>(
                new HttpResponse(httpStatus.value(), httpStatus, httpStatus.getReasonPhrase().toUpperCase(), message),
                httpStatus);
    }
}
