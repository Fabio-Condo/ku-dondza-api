package com.fabiocondo.controller;

import com.fabiocondo.domain.HttpResponse;
import com.fabiocondo.domain.OnlineCourseContent;
import com.fabiocondo.enumeration.ContentType;
import com.fabiocondo.exception.domain.CourseContentNotFoundException;
import com.fabiocondo.exception.domain.ModuleNotFoundException;
import com.fabiocondo.service.impl.OnlineCourseContentService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/online-course-content")
public class OnlineCourseContentController {

    public OnlineCourseContentService onlineCourseContentService;

    public OnlineCourseContentController(OnlineCourseContentService onlineCourseContentService) {
        this.onlineCourseContentService = onlineCourseContentService;
    }

    @PostMapping
    public ResponseEntity<OnlineCourseContent> save(@RequestParam("description") String description,
                                                    @RequestParam("contentType") ContentType contentType,
                                                    @RequestParam("time") String time,
                                                    @RequestParam("moduleId") Long moduleId,
                                                    @RequestParam("position") Integer position,
                                                    @RequestParam("file") MultipartFile file) throws ModuleNotFoundException {

        return ResponseEntity.status(HttpStatus.OK).body(onlineCourseContentService.save(description, contentType, time, moduleId, position, file));
    }

    @PutMapping
    public ResponseEntity<OnlineCourseContent> update(@RequestParam("id") Long id,
                                                      @RequestParam("description") String description,
                                                      @RequestParam("contentType") ContentType contentType,
                                                      @RequestParam("time") String time,
                                                      @RequestParam("moduleId") Long moduleId,
                                                      @RequestParam("position") Integer position,
                                                      @RequestParam(value = "file", required = false) MultipartFile file) throws CourseContentNotFoundException, ModuleNotFoundException {

        return ResponseEntity.status(HttpStatus.OK).body(onlineCourseContentService.update(id, description, contentType, time, moduleId, position, file));
    }

    @GetMapping("/findByModuleId")
    public ResponseEntity<Page<OnlineCourseContent>> findByModuleId(@RequestParam Long moduleId, Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(onlineCourseContentService.findByModuleId(moduleId, pageable));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") Long id) throws CourseContentNotFoundException {
        onlineCourseContentService.delete(id);
        return response(HttpStatus.OK, "Content deleted successfully");
    }

    @GetMapping("/download/{id}/{fileName}")
    public ResponseEntity<ByteArrayResource> downloadFile(@PathVariable Long id, @PathVariable String fileName) throws CourseContentNotFoundException {
        byte[] data = onlineCourseContentService.downloadFile(id, fileName);
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
        return ResponseEntity.status(HttpStatus.OK).body(onlineCourseContentService.getTotal());
    }

    private ResponseEntity<HttpResponse> response(HttpStatus httpStatus, String message) {
        return new ResponseEntity<>(
                new HttpResponse(httpStatus.value(), httpStatus, httpStatus.getReasonPhrase().toUpperCase(), message),
                httpStatus);
    }
}
