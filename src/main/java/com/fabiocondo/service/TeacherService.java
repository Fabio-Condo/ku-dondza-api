package com.fabiocondo.service;

import com.fabiocondo.aws.model.S3UploadResponse;
import com.fabiocondo.aws.service.AmazonS3Service;
import com.fabiocondo.domain.Exame;
import com.fabiocondo.domain.Institution;
import com.fabiocondo.domain.Teacher;
import com.fabiocondo.exception.domain.ExameNotFoundException;
import com.fabiocondo.exception.domain.InstituicaoNotFoundException;
import com.fabiocondo.exception.domain.TeacherNotFoundException;
import com.fabiocondo.repository.TeacherRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;

@Service
public class TeacherService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final String BUCKET_NAME = "b-tests-bucket";

    public TeacherRepository teacherRepository;

    private final AmazonS3Service amazonS3Service;

    public TeacherService(TeacherRepository teacherRepository, AmazonS3Service amazonS3Service) {
        this.teacherRepository = teacherRepository;
        this.amazonS3Service = amazonS3Service;
    }

    public Teacher findById(Long id) throws TeacherNotFoundException {
        logger.info("Getting course by id: " + id);
        return teacherRepository.findById(id)
                .orElseThrow(() -> new TeacherNotFoundException("No teacher found by id: " + id));
    }

    public Page<Teacher> findAll(Pageable pageable) {
        return teacherRepository.findAll(pageable);
    }

    public Page<Teacher> findByName(String name, Pageable pageable) {
        return teacherRepository.findByName(name, pageable);
    }

    public Teacher save(String name, String email, MultipartFile file) {
        logger.info("Uploading file: " + file.getOriginalFilename());
        S3UploadResponse s3UploadResponse = amazonS3Service.uploadFile(file, BUCKET_NAME);

        Teacher teacher = new Teacher();
        teacher.setName(name);
        teacher.setEmail(email);
        teacher.setUrlFile(s3UploadResponse.getFileUrl());
        teacher.setFileName(file.getOriginalFilename());

        logger.info("Saving new teacher: " + teacher.getName());
        return teacherRepository.save(teacher);
    }

    public Teacher update(Long id, String name, String email, MultipartFile file) throws TeacherNotFoundException {
        Teacher existTeacher = findById(id);
        existTeacher.setName(name);
        existTeacher.setEmail(email);

        // Se um novo arquivo é fornecido, atualiza o arquivo no serviço Amazon S3 e atualiza o nome e a URL do arquivo
        if (file != null) {
            if (existTeacher.getFileName() != null) {
                logger.info("Deleting file: " + existTeacher.getFileName());
                amazonS3Service.deleteFile(existTeacher.getFileName(), BUCKET_NAME);
            }
            S3UploadResponse s3UploadResponse = amazonS3Service.uploadFile(file, BUCKET_NAME);
            existTeacher.setUrlFile(s3UploadResponse.getFileUrl());
            existTeacher.setFileName(file.getOriginalFilename());
        }

        logger.info("Saving new teacher: " + existTeacher.getName());
        return teacherRepository.save(existTeacher);
    }

    public void delete(Long id) throws TeacherNotFoundException {
        Teacher existTeacher = findById(id);
        logger.info("Deleting exame: " + existTeacher.getName());
        teacherRepository.deleteById(id);
        if (existTeacher.getFileName() != null) {
            logger.info("Deleting file: " + existTeacher.getFileName());
            amazonS3Service.deleteFile(existTeacher.getFileName(), BUCKET_NAME);
        }
    }

    public long getTotal(){
        logger.info("Total teacher: " + teacherRepository.count());
        return teacherRepository.count();
    }

}
