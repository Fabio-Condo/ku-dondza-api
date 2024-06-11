package com.fabiocondo.aws.controller;

import com.amazonaws.services.s3.model.DeleteObjectsResult;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.fabiocondo.aws.model.S3ObjectFilter;
import com.fabiocondo.aws.model.S3UploadResponse;
import com.fabiocondo.aws.service.AmazonS3Service;
import com.fabiocondo.domain.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/s3/{bucketName}")
public class AmazonS3Controller {

    public static final String FILE_DELETED_SUCCESSFULLY = "File deleted successfully";
    public static final String SOME_FILES_NOT_DELETED_SUCCESSFULLY = "Alguns arquivos não puderam ser excluídos!";

    @Autowired
    private AmazonS3Service amazonS3Service;

    @GetMapping()
    public ResponseEntity<ListObjectsV2Result> listFiles(S3ObjectFilter s3ObjectFilter, @PathVariable String bucketName) {
        return ResponseEntity.status(HttpStatus.OK).body(amazonS3Service.listFiles(s3ObjectFilter, bucketName));
    }

    @PostMapping
    public ResponseEntity<S3UploadResponse> uploadFile(@RequestParam(value = "file") MultipartFile file, @PathVariable String bucketName) throws IOException {
        return ResponseEntity.status(HttpStatus.OK).body(amazonS3Service.uploadFile(file, bucketName));
    }

    @PostMapping("/v2")
    public ResponseEntity<S3ObjectSummary> uploadFile_V2(@RequestParam(value = "file") MultipartFile file, @PathVariable String bucketName) throws IOException {
        return ResponseEntity.status(HttpStatus.OK).body(amazonS3Service.uploadFile_V2(file, bucketName));
    }

    @GetMapping("/{fileName}")
    public ResponseEntity<ByteArrayResource> downloadFile(@PathVariable String fileName, @PathVariable String bucketName) {
        byte[] data = amazonS3Service.downloadFile(fileName, bucketName);
        ByteArrayResource resource = new ByteArrayResource(data);

        return ResponseEntity
                .status(HttpStatus.OK)
                .contentLength(data.length)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(resource);
    }

    @DeleteMapping("/{fileName}")
    public ResponseEntity<?> deleteFile(@PathVariable String fileName, @PathVariable String bucketName) {
        amazonS3Service.deleteFile(fileName, bucketName);
        return response(HttpStatus.OK, FILE_DELETED_SUCCESSFULLY);
    }

    @PostMapping("/multiple")
    public ResponseEntity<?> deleteFiles(@RequestBody List<String> fileKeys, @PathVariable String bucketName) {
        DeleteObjectsResult deleteResult = amazonS3Service.deleteFiles(fileKeys, bucketName);

        // Verificar se a exclusão foi bem-sucedida
        if (deleteResult.getDeletedObjects().size() == fileKeys.size()) {
            return response(HttpStatus.OK, FILE_DELETED_SUCCESSFULLY);
        } else {
            return response(HttpStatus.OK, SOME_FILES_NOT_DELETED_SUCCESSFULLY);
        }
    }

    @PutMapping("/replace")
    public ResponseEntity<S3UploadResponse> replaceFile(@RequestParam MultipartFile file,
                                                        @RequestParam String fileName,
                                                        @PathVariable String bucketName) {

        S3UploadResponse response = amazonS3Service.replaceFile(file, fileName, bucketName);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/filenames")
    public ResponseEntity<List<String>> listFileNames(@PathVariable String bucketName) {
        List<String> arquivos = amazonS3Service.listFileNames(bucketName);
        return ResponseEntity.ok(arquivos);
    }

    private ResponseEntity<HttpResponse> response(HttpStatus httpStatus, String message) {
        return new ResponseEntity<>(
                new HttpResponse(httpStatus.value(), httpStatus, httpStatus.getReasonPhrase().toUpperCase(), message), httpStatus);
    }
}

