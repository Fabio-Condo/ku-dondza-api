package com.fabiocondo.service;

import com.fabiocondo.aws.model.S3UploadResponse;
import com.fabiocondo.aws.service.AmazonS3Service;
import com.fabiocondo.domain.Institution;
import com.fabiocondo.enumeration.AdministrationType;
import com.fabiocondo.exception.domain.ExameNotFoundException;
import com.fabiocondo.exception.domain.InstituicaoNotFoundException;
import com.fabiocondo.repository.InstitutionRepository;
import com.fabiocondo.repository.filter.InstitutionFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class InstitutionService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final String BUCKET_NAME = "b-tests-bucket";

    InstitutionRepository institutionRepository;

    private final AmazonS3Service amazonS3Service;

    public InstitutionService(InstitutionRepository institutionRepository, AmazonS3Service amazonS3Service) {
        this.institutionRepository = institutionRepository;
        this.amazonS3Service = amazonS3Service;
    }

    public Institution findById(Long id) throws InstituicaoNotFoundException {
        logger.info("Getting institution by id: " + id);
        return institutionRepository.findById(id)
                .orElseThrow(() -> new InstituicaoNotFoundException("No institution found by id: " + id));
    }

    public Page<Institution> findAll(Pageable pageable) {
        return institutionRepository.findAll(pageable);
    }

    public Page<Institution> filter(InstitutionFilter institutionFilter, Pageable pageable){
        return institutionRepository.filter(institutionFilter, pageable);
    }

    public Institution save(String name, String type, AdministrationType administrationType, String address, String description, MultipartFile file) throws InstituicaoNotFoundException {
        logger.info("Uploading file: " + file.getOriginalFilename());
        S3UploadResponse s3UploadResponse = amazonS3Service.uploadFile(file, BUCKET_NAME);

        Institution institution = new Institution();
        institution.setName(name);
        institution.setType(type);
        institution.setAdministrationType(administrationType);
        institution.setAddress(address);
        institution.setDescription(description);
        institution.setUrlFile(s3UploadResponse.getFileUrl());
        institution.setFileName(file.getOriginalFilename());

        logger.info("Saving new institution: " + institution.getName());
        return institutionRepository.save(institution);
    }

    public Institution update(Long id, String name, String type, AdministrationType administrationType, String address, String description, MultipartFile file) throws ExameNotFoundException, InstituicaoNotFoundException {
        Institution existInstitution = findById(id);
        existInstitution.setName(name);
        existInstitution.setType(type);
        existInstitution.setAdministrationType(administrationType);
        existInstitution.setAddress(address);
        existInstitution.setDescription(description);

        // Se um novo arquivo é fornecido, atualiza o arquivo no serviço Amazon S3 e atualiza o nome e a URL do arquivo
        if (file != null) {
            if (existInstitution.getFileName() != null) {
                logger.info("Deleting file: " + existInstitution.getFileName());
                amazonS3Service.deleteFile(existInstitution.getFileName(), BUCKET_NAME);
            }
            S3UploadResponse s3UploadResponse = amazonS3Service.uploadFile(file, BUCKET_NAME);
            existInstitution.setUrlFile(s3UploadResponse.getFileUrl());
            existInstitution.setFileName(file.getOriginalFilename());
        }

        logger.info("Saving new institution: " + existInstitution.getDescription());
        return institutionRepository.save(existInstitution);
    }

    public void delete(Long id) throws InstituicaoNotFoundException {
        Institution existInstitution = findById(id);
        logger.info("Deleting institution: " + existInstitution.getDescription());
        institutionRepository.deleteById(id);
        if (existInstitution.getFileName() != null) {
            logger.info("Deleting file: " + existInstitution.getFileName());
            amazonS3Service.deleteFile(existInstitution.getFileName(), BUCKET_NAME);
        }
    }

    public long getTotal(){
        logger.info("Total institution: " + institutionRepository.count());
        return institutionRepository.count();
    }
}
