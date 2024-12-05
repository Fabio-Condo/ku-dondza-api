package com.fabiocondo.service.impl;

import com.fabiocondo.aws.model.S3UploadResponse;
import com.fabiocondo.aws.service.AmazonS3Service;
import com.fabiocondo.domain.OnlineCourse;
import com.fabiocondo.domain.Tema;
import com.fabiocondo.exception.domain.CourseContentNotFoundException;
import com.fabiocondo.exception.domain.CourseNotFoundException;
import com.fabiocondo.exception.domain.TemaNotFoundException;
import com.fabiocondo.repository.TemaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class TemaService {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final TemaRepository temaRepository;
    private final AmazonS3Service amazonS3Service;
    private final OnlineCourseService onlineCourseService;
    private static final String BUCKET_NAME = "b-tests-bucket";

    public TemaService(TemaRepository temaRepository, AmazonS3Service amazonS3Service, OnlineCourseService onlineCourseService) {
        this.temaRepository = temaRepository;
        this.amazonS3Service = amazonS3Service;
        this.onlineCourseService = onlineCourseService;
    }

    public Tema findById(Long id) throws TemaNotFoundException {
        logger.info("Getting tema by id: " + id);
        return temaRepository.findById(id)
                .orElseThrow(() -> new TemaNotFoundException("No tema found by id: " + id));
    }

    public Page<Tema> findByOnlineCourseId(Long courseId, Pageable pageable) {
        return temaRepository.findByOnlineCourseId(courseId, pageable);
    }

    public List<Tema> findByOnlineCourseId(Long courseId) {
        return temaRepository.findByOnlineCourseId(courseId);
    }

    public Page<Tema> findAll(String searchParam, Pageable pageable) {
        return temaRepository.findAll(searchParam, pageable);
    }

    public List<Tema> findAll() {
        return temaRepository.findAll();
    }

    public Tema save(String name, Long onlineCourseId, MultipartFile file) throws CourseNotFoundException, TemaNotFoundException {
        logger.info("Uploading file: " + file.getOriginalFilename());
        S3UploadResponse s3UploadResponse = amazonS3Service.uploadFile(file, BUCKET_NAME);

        OnlineCourse onlineCourse = onlineCourseService.findById(onlineCourseId);

        Tema tema = new Tema();
        tema.setName(name);
        tema.setUrlFile(s3UploadResponse.getFileUrl());
        tema.setFileName(file.getOriginalFilename());
        tema.setOnlineCourse(onlineCourse);

        logger.info("Saving new tema: " + tema.getName());
        return temaRepository.save(tema);
    }

    public Tema update(Long id, String name, Long onlineCourseId, MultipartFile file) throws CourseContentNotFoundException, CourseNotFoundException, TemaNotFoundException {
        OnlineCourse onlineCourse = onlineCourseService.findById(onlineCourseId);

        Tema existTema = findById(id);
        existTema.setOnlineCourse(onlineCourse);
        existTema.setName(name);
        // Se um novo arquivo é fornecido, atualiza o arquivo no serviço Amazon S3 e atualiza o nome e a URL do arquivo
        if (file != null) {
            if (existTema.getFileName() != null) {
                logger.info("Deleting file: " + existTema.getFileName());
                amazonS3Service.deleteFile(existTema.getFileName(), BUCKET_NAME);
            }
            S3UploadResponse s3UploadResponse = amazonS3Service.uploadFile(file, BUCKET_NAME);
            existTema.setUrlFile(s3UploadResponse.getFileUrl());
            existTema.setFileName(file.getOriginalFilename());
        }

        logger.info("Updating tema: " + existTema.getName());
        return temaRepository.save(existTema);
    }

    public void delete(Long id) throws TemaNotFoundException {
        Tema existTema = findById(id);
        temaRepository.deleteById(id);
    }

    public long getTotal(){
        return temaRepository.count();
    }
}
