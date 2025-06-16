package com.fabiocondo.aws.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.IOUtils;
import com.fabiocondo.aws.model.S3UploadResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Service
public class AmazonS3Service {

    private final AmazonS3 amazonS3;

    @Value("${spring.servlet.multipart.max-file-size}")
    private String maxFileSizeString;

    public AmazonS3Service(AmazonS3 amazonS3) {
        this.amazonS3 = amazonS3;
    }

    public S3UploadResponse uploadFile(MultipartFile file, String bucketName, String keyName) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(file.getContentType());
        metadata.setContentLength(file.getSize());
        metadata.setCacheControl("public, max-age=31536000");

        try {
            PutObjectRequest putRequest = new PutObjectRequest(bucketName, keyName, file.getInputStream(), metadata);
            amazonS3.putObject(putRequest);

            return new S3UploadResponse(
                    keyName,
                    bucketName,
                    file.getSize(),
                    LocalDateTime.now(),
                    getUrl(keyName, bucketName)
            );
        } catch (IOException e) {
            throw new RuntimeException("Erro ao fazer upload do arquivo para o S3", e);
        }
    }


    public S3UploadResponse uploadFile2(MultipartFile file, String bucketName) {
        String fileName = UUID.randomUUID() + "-" + file.getOriginalFilename();

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(file.getContentType());
        metadata.setCacheControl("public, max-age=31536000");

        try {
            PutObjectRequest putRequest = new PutObjectRequest(bucketName, fileName, file.getInputStream(), metadata);
            amazonS3.putObject(putRequest);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao enviar arquivo para o S3", e);
        }

        return new S3UploadResponse(
                fileName,
                bucketName,
                file.getSize(),
                LocalDateTime.now(),
                getUrl(fileName, bucketName)
        );
    }

    public byte[] downloadFile(String fileName, String bucketName) {
        S3Object s3Object = amazonS3.getObject(bucketName, fileName);
        S3ObjectInputStream inputStream = s3Object.getObjectContent();
        try {
            return IOUtils.toByteArray(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void deleteFile(String fileName, String bucketName) {
        amazonS3.deleteObject(bucketName, fileName);
    }

    private File convertMultiPartFileToFile(MultipartFile file) {
        File convertedFile = new File(Objects.requireNonNull(file.getOriginalFilename()));
        try (FileOutputStream fos = new FileOutputStream(convertedFile)) {
            fos.write(file.getBytes());
        } catch (IOException e) {
            //log.error("Error converting multipartFile to file", e);
        }
        return convertedFile;
    }

    private String getUrl(String key, String bucketName) {
        return amazonS3.getUrl(bucketName, key).toString();
    }

}


// Poilitica que torna os objectos do bucket visiveis ao publico
    /*
    {
        "Version": "2012-10-17",
            "Statement": [
        {
            "Sid": "PublicReadGetObject",
                "Effect": "Allow",
                "Principal": "*",
                "Action": "s3:GetObject",
                "Resource": "arn:aws:s3:::nome-do-seu-bucket/*"
        }
    ]
    }
     */
