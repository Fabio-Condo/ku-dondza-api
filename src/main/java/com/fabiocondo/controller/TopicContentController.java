package com.fabiocondo.controller;

import com.fabiocondo.domain.HttpResponse;
import com.fabiocondo.domain.TopicContent;
import com.fabiocondo.enumeration.ContentType;
import com.fabiocondo.exception.domain.ContentNotFoundException;
import com.fabiocondo.exception.domain.DownloadRateLimitExceededException;
import com.fabiocondo.exception.domain.ModuleNotFoundException;
import com.fabiocondo.exception.domain.TopicNotFoundException;
import com.fabiocondo.service.impl.TopicContentService;
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
@RequestMapping("/topic-content")
public class TopicContentController {

    public TopicContentService topicContentService;

    public TopicContentController(TopicContentService topicContentService) {
        this.topicContentService = topicContentService;
    }

    @PostMapping
    public ResponseEntity<TopicContent> save(@RequestParam("description") String description,
                                             @RequestParam("contentType") ContentType contentType,
                                             @RequestParam("time") String time,
                                             @RequestParam("topicId") Long topicId,
                                             @RequestParam("position") Integer position,
                                             @RequestParam("file") MultipartFile file) throws ModuleNotFoundException, TopicNotFoundException {

        return ResponseEntity.status(HttpStatus.OK).body(topicContentService.save(description, contentType, time, topicId, position, file));
    }

    @PutMapping
    public ResponseEntity<TopicContent> update(@RequestParam("id") Long id,
                                          @RequestParam("description") String description,
                                          @RequestParam("contentType") ContentType contentType,
                                          @RequestParam("time") String time,
                                          @RequestParam("topicId") Long topicId,
                                          @RequestParam("position") Integer position,
                                          @RequestParam(value = "file", required = false) MultipartFile file) throws ContentNotFoundException, TopicNotFoundException {

        return ResponseEntity.status(HttpStatus.OK).body(topicContentService.update(id, description, contentType, time, topicId, position, file));
    }

    @GetMapping("/findByTopicId")
    public ResponseEntity<Page<TopicContent>> findByTopicId(@RequestParam Long topicId, Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(topicContentService.findByTopicId(topicId, pageable));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") Long id) throws ContentNotFoundException {
        topicContentService.delete(id);
        return response(HttpStatus.OK, "Content deleted successfully");
    }

    @GetMapping("/download/{id}/{fileName}")
    public ResponseEntity<ByteArrayResource> downloadFile(@PathVariable Long id, @PathVariable String fileName) throws ContentNotFoundException {
        byte[] data = topicContentService.downloadFile(id, fileName);
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
        return ResponseEntity.status(HttpStatus.OK).body(topicContentService.getTotal());
    }

    private ResponseEntity<HttpResponse> response(HttpStatus httpStatus, String message) {
        return new ResponseEntity<>(
                new HttpResponse(httpStatus.value(), httpStatus, httpStatus.getReasonPhrase().toUpperCase(), message),
                httpStatus);
    }
}
