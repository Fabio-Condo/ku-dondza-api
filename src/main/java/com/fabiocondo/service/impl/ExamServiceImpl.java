package com.fabiocondo.service.impl;

import com.fabiocondo.aws.model.S3UploadResponse;
import com.fabiocondo.aws.service.AmazonS3Service;
import com.fabiocondo.domain.Exam;
import com.fabiocondo.domain.Institution;
import com.fabiocondo.domain.Subject;
import com.fabiocondo.enumeration.ExamStatus;
import com.fabiocondo.exception.domain.ExamNotFoundException;
import com.fabiocondo.exception.domain.InstituicaoNotFoundException;
import com.fabiocondo.exception.domain.SubjectNotFoundException;
import com.fabiocondo.repository.ExamRepository;
import com.fabiocondo.repository.filter.ExamFilter;
import com.fabiocondo.service.ExamService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;

@Service
public class ExamServiceImpl implements ExamService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final String BUCKET_NAME = "b-tests-bucket";

    public ExamRepository examRepository;

    private final AmazonS3Service amazonS3Service;

    private final InstitutionServiceImpl institutionServiceImpl;

    private final SubjectServiceImpl subjectServiceImpl;

    @Autowired
    public ExamServiceImpl(ExamRepository examRepository, AmazonS3Service amazonS3Service, InstitutionServiceImpl institutionServiceImpl, SubjectServiceImpl subjectServiceImpl) {
        this.examRepository = examRepository;
        this.amazonS3Service = amazonS3Service;
        this.institutionServiceImpl = institutionServiceImpl;
        this.subjectServiceImpl = subjectServiceImpl;
    }

    @Override
    public Exam findById(Long id) throws ExamNotFoundException {
        logger.info("Getting exame by id: " + id);
        return examRepository.findById(id)
                .orElseThrow(() -> new ExamNotFoundException("No exame found by id: " + id));
    }

    @Override
    public Page<Exam> filter(ExamFilter examFilter, Pageable pageable) {
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
    public Exam save(String description, ExamStatus status, Date date, Long subjectId, Long institutionId, MultipartFile file) throws InstituicaoNotFoundException, SubjectNotFoundException {
        logger.info("Uploading file: " + file.getOriginalFilename());
        S3UploadResponse s3UploadResponse = amazonS3Service.uploadFile(file, BUCKET_NAME);

        Institution institution = institutionServiceImpl.findById(institutionId);
        Subject subject = subjectServiceImpl.findById(subjectId);

        Exam exam = new Exam();
        exam.setInstitution(institution);
        exam.setSubject(subject);
        exam.setDescription(description);
        exam.setStatus(status);
        exam.setDate(date);
        exam.setTotalDownloadNumber(0L);
        exam.setUrlFile(s3UploadResponse.getFileUrl());
        exam.setFileName(file.getOriginalFilename());

        logger.info("Saving new exam: " + exam.getDescription());
        return examRepository.save(exam);
    }

    @Override
    public Exam update(Long id, String description, ExamStatus status, Date date, Long subjectId, Long institutionId, MultipartFile file) throws ExamNotFoundException, InstituicaoNotFoundException, SubjectNotFoundException {
        Institution institution = institutionServiceImpl.findById(institutionId);
        Subject subject = subjectServiceImpl.findById(subjectId);

        Exam existExam = findById(id);
        existExam.setInstitution(institution);
        existExam.setSubject(subject);
        existExam.setDescription(description);
        existExam.setStatus(status);
        existExam.setDate(date);

        // Se um novo arquivo é fornecido, atualiza o arquivo no serviço Amazon S3 e atualiza o nome e a URL do arquivo
        if (file != null) {
            if (existExam.getFileName() != null) {
                logger.info("Deleting file: " + existExam.getFileName());
                amazonS3Service.deleteFile(existExam.getFileName(), BUCKET_NAME);
            }
            S3UploadResponse s3UploadResponse = amazonS3Service.uploadFile(file, BUCKET_NAME);
            existExam.setUrlFile(s3UploadResponse.getFileUrl());
            existExam.setFileName(file.getOriginalFilename());
        }

        logger.info("Saving new exame: " + existExam.getDescription());
        return examRepository.save(existExam);
    }

    @Override
    public void delete(Long id) throws ExamNotFoundException {
        Exam existExam = findById(id);
        logger.info("Deleting exame: " + existExam.getDescription());
        examRepository.deleteById(id);
        if (existExam.getFileName() != null) {
            logger.info("Deleting file: " + existExam.getFileName());
            amazonS3Service.deleteFile(existExam.getFileName(), BUCKET_NAME);
        }
    }

    @Override
    public byte[] downloadFile(Long id, @PathVariable String fileName) throws ExamNotFoundException {
        Exam existExam = findById(id);
        logger.info("Downloading file: " + existExam.getFileName());
        byte[] data = amazonS3Service.downloadFile(fileName, BUCKET_NAME);
        existExam.setTotalDownloadNumber(existExam.getTotalDownloadNumber() + 1);
        examRepository.save(existExam);
        return data;
    }

    @Override
    public long getTotal(){
        logger.info("Total exames: " + examRepository.count());
        return examRepository.count();
    }
}
