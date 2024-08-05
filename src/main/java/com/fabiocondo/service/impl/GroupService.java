package com.fabiocondo.service.impl;

import com.fabiocondo.aws.model.S3UploadResponse;
import com.fabiocondo.aws.service.AmazonS3Service;
import com.fabiocondo.domain.*;
import com.fabiocondo.exception.domain.ExameNotFoundException;
import com.fabiocondo.exception.domain.GroupNotFoundException;
import com.fabiocondo.exception.domain.InstituicaoNotFoundException;
import com.fabiocondo.exception.domain.SubjectNotFoundException;
import com.fabiocondo.repository.GroupRepository;
import com.fabiocondo.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class GroupService {

    private static final String BUCKET_NAME = "b-tests-bucket";
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final AmazonS3Service amazonS3Service;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;

    @Autowired
    public GroupService(AmazonS3Service amazonS3Service, GroupRepository groupRepository, UserRepository userRepository) {
        this.amazonS3Service = amazonS3Service;
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
    }

    public Group findById(Long id) throws GroupNotFoundException {
        logger.info("Getting group by id: " + id);
        return groupRepository.findById(id)
                .orElseThrow(() -> new GroupNotFoundException("No Group found by id: " + id));
    }

    public Group save(String description, MultipartFile file) {
        logger.info("Uploading file: " + file.getOriginalFilename());
        S3UploadResponse s3UploadResponse = amazonS3Service.uploadFile(file, BUCKET_NAME);

        Group group = new Group();
        group.setDescription(description);
        group.setUrlFile(s3UploadResponse.getFileUrl());
        group.setFileName(file.getOriginalFilename());

        logger.info("Saving new group: " + group.getDescription());
        return groupRepository.save(group);
    }

    public Group update(Long id, String description, MultipartFile file) throws GroupNotFoundException {

        Group existGroup = findById(id);
        existGroup.setDescription(description);

        // Se um novo arquivo é fornecido, atualiza o arquivo no serviço Amazon S3 e atualiza o nome e a URL do arquivo
        if (file != null) {
            if (existGroup.getFileName() != null) {
                logger.info("Deleting file: " + existGroup.getFileName());
                amazonS3Service.deleteFile(existGroup.getFileName(), BUCKET_NAME);
            }
            S3UploadResponse s3UploadResponse = amazonS3Service.uploadFile(file, BUCKET_NAME);
            existGroup.setUrlFile(s3UploadResponse.getFileUrl());
            existGroup.setFileName(file.getOriginalFilename());
        }

        logger.info("Updating group: " + existGroup.getDescription());
        return groupRepository.save(existGroup);
    }

    public Page<Group> findAll(Pageable pageable) {
        return groupRepository.findAll(pageable);
    }

    public void delete(Long id) throws GroupNotFoundException {
        Group existGroup = findById(id);
        logger.info("Deleting group: " + existGroup.getDescription());
        groupRepository.deleteById(id);
        if (existGroup.getFileName() != null) {
            logger.info("Deleting file: " + existGroup.getFileName());
            amazonS3Service.deleteFile(existGroup.getFileName(), BUCKET_NAME);
        }
    }

    public Group addMemberToGroup(Long groupId, Long userId) throws GroupNotFoundException {
        Group group = findById(groupId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("No user found by id: " + userId));

        group.getMembers().add(user);
        return groupRepository.save(group);
    }

    public Group removeMemberFromGroup(Long groupId, Long userId) throws GroupNotFoundException {
        Group group = findById(groupId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("No user found by id: " + userId));

        group.getMembers().remove(user);
        return groupRepository.save(group);
    }

    public long getTotal(){
        logger.info("Total groups: " + groupRepository.count());
        return groupRepository.count();
    }
}
