package com.fabiocondo.aws.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.IOUtils;
import com.fabiocondo.aws.model.S3ObjectFilter;
import com.fabiocondo.aws.model.S3UploadResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class AmazonS3Service {

    @Autowired
    private AmazonS3 amazonS3;

    @Value("${spring.servlet.multipart.max-file-size}")
    private String maxFileSizeString;

    public ListObjectsV2Result listFiles(S3ObjectFilter s3ObjectFilter, String bucketName) {
        ListObjectsV2Request request = new ListObjectsV2Request().withBucketName(bucketName);
        request.withPrefix(s3ObjectFilter.getPrefix());
        request.withMaxKeys(s3ObjectFilter.getMaxKeys());
        //request.withContinuationToken(s3ObjectFilter.getNextContinuationToken()); // O next
        return amazonS3.listObjectsV2(request);
    }

    public S3UploadResponse uploadFile(MultipartFile file, String bucketName) {
        File convertedFile = convertMultiPartFileToFile(file);
        String fileName = file.getOriginalFilename();
        amazonS3.putObject(new PutObjectRequest(bucketName, fileName, convertedFile));
        convertedFile.delete();
        return new S3UploadResponse(fileName, bucketName, file.getSize(), LocalDateTime.now(), getUrl(fileName, bucketName));
    }

    public S3ObjectSummary uploadFile_V2(MultipartFile file, String bucketName) {
        File convertedFile = convertMultiPartFileToFile(file);
        String fileName = file.getOriginalFilename();
        amazonS3.putObject(new PutObjectRequest(bucketName, fileName, convertedFile));
        convertedFile.delete();
        return getObjectSummary(bucketName, fileName);
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

    public DeleteObjectsResult deleteFiles(List<String> fileKeys, String bucketName) {
        // Criar a lista de objetos a serem excluídos
        List<DeleteObjectsRequest.KeyVersion> keys = new ArrayList<>();
        for (String fileKey : fileKeys) {
            keys.add(new DeleteObjectsRequest.KeyVersion(fileKey));
        }

        // Criar a solicitação para excluir os objetos
        DeleteObjectsRequest deleteRequest = new DeleteObjectsRequest(bucketName)
                .withKeys(keys);

        // Enviar a solicitação de exclusão para o Amazon S3
        DeleteObjectsResult deleteResult = amazonS3.deleteObjects(deleteRequest);

        return deleteResult;
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

    private S3ObjectSummary getObjectSummary(String fileName, String bucketName) {
        // Listar os objetos no bucket para obter o resumo do objeto especificado
        ListObjectsV2Request listObjectsRequest = new ListObjectsV2Request().withBucketName(bucketName).withPrefix(fileName);
        ListObjectsV2Result listObjectsResult = amazonS3.listObjectsV2(listObjectsRequest);
        List<S3ObjectSummary> objectSummaries = listObjectsResult.getObjectSummaries();
        // Retorna o primeiro resumo do objeto encontrado (deve ser único)
        if (!objectSummaries.isEmpty()) {
            return objectSummaries.get(0);
        } else {
            // Se o objeto não for encontrado, retorna null ou lança uma exceção, dependendo dos requisitos do seu aplicativo
            return null;
        }
    }

    public S3UploadResponse replaceFile(MultipartFile file, String fileName, String bucketName) {
        if (!fileExists(fileName, bucketName)) {
            throw new IllegalArgumentException("O arquivo a ser substituído não existe.");
        }
        deleteFile(fileName, bucketName);
        return uploadFile(file, bucketName);
    }

    private boolean fileExists(String fileName, String bucketName) {
        return getObjectSummary(fileName, bucketName) != null;
    }

    public List<String> listFileNames(String bucketName) {
        ListObjectsV2Request request = new ListObjectsV2Request().withBucketName(bucketName);
        List<String> fileNames = new ArrayList<>();
        ListObjectsV2Result result;

        do {
            result = amazonS3.listObjectsV2(request);

            for (S3ObjectSummary objectSummary : result.getObjectSummaries()) {
                fileNames.add(objectSummary.getKey());
            }

            request.setContinuationToken(result.getNextContinuationToken());
        } while (result.isTruncated());

        return fileNames;
    }

    private long parseFileSize(String fileSizeString) {

        System.out.println("Max file size system config: " + maxFileSizeString);

        fileSizeString = fileSizeString.trim().toUpperCase();
        long multiplier = 1;
        if (fileSizeString.endsWith("KB")) {
            multiplier = 1024;
            fileSizeString = fileSizeString.substring(0, fileSizeString.length() - 2).trim();
        } else if (fileSizeString.endsWith("MB")) {
            multiplier = 1024 * 1024;
            fileSizeString = fileSizeString.substring(0, fileSizeString.length() - 2).trim();
        } else if (fileSizeString.endsWith("GB")) {
            multiplier = 1024 * 1024 * 1024;
            fileSizeString = fileSizeString.substring(0, fileSizeString.length() - 2).trim();
        }
        // Remove espaços em branco e converte para long
        return Long.parseLong(fileSizeString) * multiplier;
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


}
