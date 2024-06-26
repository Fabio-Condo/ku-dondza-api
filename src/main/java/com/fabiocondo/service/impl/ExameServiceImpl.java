package com.fabiocondo.service.impl;

import com.fabiocondo.aws.model.S3UploadResponse;
import com.fabiocondo.aws.service.AmazonS3Service;
import com.fabiocondo.domain.Exame;
import com.fabiocondo.domain.Institution;
import com.fabiocondo.domain.Subject;
import com.fabiocondo.exception.domain.ExameNotFoundException;
import com.fabiocondo.exception.domain.InstituicaoNotFoundException;
import com.fabiocondo.exception.domain.SubjectNotFoundException;
import com.fabiocondo.repository.ExameRepository;
import com.fabiocondo.repository.filter.ExameFilter;
import com.fabiocondo.service.ExameService;
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
public class ExameServiceImpl implements ExameService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final String BUCKET_NAME = "b-tests-bucket";

    public ExameRepository exameRepository;

    private final AmazonS3Service amazonS3Service;

    private final InstitutionServiceImpl institutionServiceImpl;

    private final SubjectServiceImpl subjectServiceImpl;

    @Autowired
    public ExameServiceImpl(ExameRepository exameRepository, AmazonS3Service amazonS3Service, InstitutionServiceImpl institutionServiceImpl, SubjectServiceImpl subjectServiceImpl) {
        this.exameRepository = exameRepository;
        this.amazonS3Service = amazonS3Service;
        this.institutionServiceImpl = institutionServiceImpl;
        this.subjectServiceImpl = subjectServiceImpl;
    }

    @Override
    public Exame findById(Long id) throws ExameNotFoundException {
        logger.info("Getting exame by id: " + id);
        return exameRepository.findById(id)
                .orElseThrow(() -> new ExameNotFoundException("No exame found by id: " + id));
    }

    @Override
    public Page<Exame> filter(ExameFilter exameFilter, Pageable pageable) {
        return exameRepository.filter(exameFilter, pageable);
    }

    @Override
    public Page<Exame> findAll(Pageable pageable) {
        return exameRepository.findAll(pageable);
    }

    @Override
    public List<Exame> findAll() {
        return exameRepository.findAll();
    }

    @Override
    public Exame save(String description, Date date, Long subjectId, Long institutionId, MultipartFile file) throws InstituicaoNotFoundException, SubjectNotFoundException {
        logger.info("Uploading file: " + file.getOriginalFilename());
        S3UploadResponse s3UploadResponse = amazonS3Service.uploadFile(file, BUCKET_NAME);

        Institution institution = institutionServiceImpl.findById(institutionId);
        Subject subject = subjectServiceImpl.findById(subjectId);

        Exame exame = new Exame();
        exame.setInstitution(institution);
        exame.setSubject(subject);
        exame.setDescription(description);
        exame.setDate(date);
        exame.setTotalDownloadNumber(0L);
        exame.setUrlFile(s3UploadResponse.getFileUrl());
        exame.setFileName(file.getOriginalFilename());

        logger.info("Saving new exame: " + exame.getDescription());
        return exameRepository.save(exame);
    }

    @Override
    public Exame update(Long id, String description, Date date, Long subjectId, Long institutionId, MultipartFile file) throws ExameNotFoundException, InstituicaoNotFoundException, SubjectNotFoundException {
        Institution institution = institutionServiceImpl.findById(institutionId);
        Subject subject = subjectServiceImpl.findById(subjectId);

        Exame existExame = findById(id);
        existExame.setInstitution(institution);
        existExame.setSubject(subject);
        existExame.setDescription(description);
        existExame.setDate(date);

        // Se um novo arquivo é fornecido, atualiza o arquivo no serviço Amazon S3 e atualiza o nome e a URL do arquivo
        if (file != null) {
            if (existExame.getFileName() != null) {
                logger.info("Deleting file: " + existExame.getFileName());
                amazonS3Service.deleteFile(existExame.getFileName(), BUCKET_NAME);
            }
            S3UploadResponse s3UploadResponse = amazonS3Service.uploadFile(file, BUCKET_NAME);
            existExame.setUrlFile(s3UploadResponse.getFileUrl());
            existExame.setFileName(file.getOriginalFilename());
        }

        logger.info("Saving new exame: " + existExame.getDescription());
        return exameRepository.save(existExame);
    }

    @Override
    public void delete(Long id) throws ExameNotFoundException {
        Exame existExame = findById(id);
        logger.info("Deleting exame: " + existExame.getDescription());
        exameRepository.deleteById(id);
        if (existExame.getFileName() != null) {
            logger.info("Deleting file: " + existExame.getFileName());
            amazonS3Service.deleteFile(existExame.getFileName(), BUCKET_NAME);
        }
    }

    @Override
    public byte[] downloadFile(Long id, @PathVariable String fileName) throws ExameNotFoundException {
        Exame existExame = findById(id);
        logger.info("Downloading file: " + existExame.getFileName());
        byte[] data = amazonS3Service.downloadFile(fileName, BUCKET_NAME);
        existExame.setTotalDownloadNumber(existExame.getTotalDownloadNumber() + 1);
        exameRepository.save(existExame);
        return data;
    }

    @Override
    public long getTotal(){
        logger.info("Total exames: " + exameRepository.count());
        return exameRepository.count();
    }
}
