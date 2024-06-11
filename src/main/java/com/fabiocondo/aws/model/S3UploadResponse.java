package com.fabiocondo.aws.model;

import java.time.LocalDateTime;

public class S3UploadResponse {

    private String fileName;
    private String bucketName;
    private long fileSize;
    private LocalDateTime uploadTime;
    private String fileUrl;

    public S3UploadResponse(String fileName, String bucketName, long fileSize, LocalDateTime uploadTime, String fileUrl) {
        this.fileName = fileName;
        this.bucketName = bucketName;
        this.fileSize = fileSize;
        this.uploadTime = uploadTime;
        this.fileUrl = fileUrl;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public LocalDateTime getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(LocalDateTime uploadTime) {
        this.uploadTime = uploadTime;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }
}
