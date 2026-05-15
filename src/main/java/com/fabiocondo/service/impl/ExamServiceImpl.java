package com.fabiocondo.service.impl;

import com.fabiocondo.aws.model.S3UploadResponse;
import com.fabiocondo.aws.service.AmazonS3Service;
import com.fabiocondo.domain.*;
import com.fabiocondo.enumeration.ContentType;
import com.fabiocondo.enumeration.ExamType;
import com.fabiocondo.enumeration.Institution;
import com.fabiocondo.exception.domain.ExamNotFoundException;
import com.fabiocondo.exception.domain.QuestionNotFoundException;
import com.fabiocondo.exception.domain.SubjectNotFoundException;
import com.fabiocondo.exception.domain.TopicNotFoundException;
import com.fabiocondo.repository.ExamRepository;
import com.fabiocondo.repository.filter.ExamFilter;
import com.fabiocondo.service.ExamService;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class ExamServiceImpl implements ExamService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final String BUCKET_NAME = "dikahub-exames-bucket";

    public ExamRepository examRepository;

    private final AmazonS3Service amazonS3Service;

    private final SubjectServiceImpl subjectServiceImpl;

    @Autowired
    public ExamServiceImpl(ExamRepository examRepository, AmazonS3Service amazonS3Service, SubjectServiceImpl subjectServiceImpl) {
        this.examRepository = examRepository;
        this.amazonS3Service = amazonS3Service;
        this.subjectServiceImpl = subjectServiceImpl;
    }

    @Override
    public Exam findById(Long id) throws ExamNotFoundException {
        logger.info("Getting exame by id: " + id);
        return examRepository.findById(id)
                .orElseThrow(() -> new ExamNotFoundException("No exame found by id: " + id));
    }

    @Override
    @Cacheable("exams")
    public Page<Exam> filter(ExamFilter examFilter, Pageable pageable) {
        logger.info("Getting exams");
        return examRepository.filter(examFilter, pageable);
    }

    @Override
    public Page<Exam> findAll(Pageable pageable) {
        return examRepository.findAll(pageable);
    }

    @Override
    public List<Exam> findAll() {
        return examRepository.findAll();
    }

    @Override
    public Exam save(ExamType examType, Institution institution, boolean premium, Long year, Long subjectId, String number, MultipartFile file) throws SubjectNotFoundException {
        logger.info("Uploading file: " + file.getOriginalFilename());
        //String fileKey = UUID.randomUUID() + "-" + file.getOriginalFilename();
        String fileKey = "exame_" + RandomStringUtils.randomNumeric(4).toLowerCase() + "_" + file.getOriginalFilename();
        S3UploadResponse s3UploadResponse = amazonS3Service.uploadFile(file, BUCKET_NAME, fileKey);

        Subject subject = subjectServiceImpl.findById(subjectId);

        Exam exam = new Exam();
        exam.setSubject(subject);
        exam.setExamType(examType);
        exam.setInstitution(institution);
        exam.setPremium(premium);
        exam.setNumber(number);
        exam.setYear(year);
        exam.setTotalDownloadNumber(0L);
        exam.setUrlFile(s3UploadResponse.getFileUrl());
        exam.setFileName(fileKey);

        logger.info("Saving new exam: " + exam.getExamType());
        return examRepository.save(exam);
    }

    @Override
    public Exam update(Long id, ExamType examType, Institution institution, boolean premium, Long year, Long subjectId, String number, MultipartFile file) throws SubjectNotFoundException, ExamNotFoundException {
        Subject subject = subjectServiceImpl.findById(subjectId);

        Exam existExam = findById(id);
        existExam.setSubject(subject);
        existExam.setExamType(examType);
        existExam.setInstitution(institution);
        existExam.setPremium(premium);
        existExam.setNumber(number);
        existExam.setYear(year);

        // Se um novo arquivo é fornecido, atualiza o arquivo no serviço Amazon S3 e atualiza o nome e a URL do arquivo
        if (file != null) {
            if (existExam.getFileName() != null) {
                logger.info("Deleting file: " + existExam.getFileName());
                amazonS3Service.deleteFile(existExam.getFileName(), BUCKET_NAME);
            }
            //String newFileKey = UUID.randomUUID() + "-" + file.getOriginalFilename();
            String newFileKey = "exame_" + RandomStringUtils.randomNumeric(4).toLowerCase() + "_" + file.getOriginalFilename();
            S3UploadResponse s3UploadResponse = amazonS3Service.uploadFile(file, BUCKET_NAME, newFileKey);
            existExam.setUrlFile(s3UploadResponse.getFileUrl());
            existExam.setFileName(newFileKey);
        }

        logger.info("Saving new exame: " + existExam.getExamType());
        return examRepository.save(existExam);
    }

    @Override
    public void delete(Long id) throws ExamNotFoundException {
        Exam existExam = findById(id);
        logger.info("Deleting exame: " + existExam.getExamType());
        examRepository.deleteById(id);
        if (existExam.getFileName() != null) {
            logger.info("Deleting file: " + existExam.getFileName());
            amazonS3Service.deleteFile(existExam.getFileName(), BUCKET_NAME);
        }
    }

    @Override
    public byte[] downloadFile(Long id, @PathVariable String fileName) throws ExamNotFoundException {
        Exam existExam = findById(id);
        logger.info("Downloading file 1: " + fileName);
        logger.info("Downloading file: " + existExam.getFileName());
        byte[] data = amazonS3Service.downloadFile(fileName, BUCKET_NAME);
        //existExam.setTotalDownloadNumber(existExam.getTotalDownloadNumber() + 1);
        //examRepository.save(existExam);
        return data;
    }

    @Override
    public long getTotal(){
        logger.info("Total exames: " + examRepository.count());
        return examRepository.count();
    }
}