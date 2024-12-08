package com.fabiocondo.service.impl;

import com.fabiocondo.aws.model.S3UploadResponse;
import com.fabiocondo.aws.service.AmazonS3Service;
import com.fabiocondo.domain.*;
import com.fabiocondo.exception.domain.GroupNotFoundException;
import com.fabiocondo.exception.domain.UserNotFoundException;
import com.fabiocondo.repository.GroupRepository;
import com.fabiocondo.repository.UserRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

import static com.fabiocondo.constant.UserImplConstant.FOUND_USER_BY_USERNAME;
import static com.fabiocondo.constant.UserImplConstant.NO_USER_FOUND_BY_USERNAME;

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

    public Group findGroupByGroupId(String groupId) throws GroupNotFoundException {
        return groupRepository.findGroupByGroupId(groupId)
                .orElseThrow(() -> new GroupNotFoundException("No Group found by id: " + groupId));
    }

    public Group save(String name, String description, MultipartFile file) throws UserNotFoundException {
        logger.info("Uploading file: " + file.getOriginalFilename());
        S3UploadResponse s3UploadResponse = amazonS3Service.uploadFile(file, BUCKET_NAME);

        Group group = new Group();
        group.setGroupId(generateGroupId());
        group.setName(name);
        group.setDescription(description);
        group.setUrlFile(s3UploadResponse.getFileUrl());
        group.setFileName(file.getOriginalFilename());
        group.setCreator(getAuthenticatedUser());
        group.getMembers().add(getAuthenticatedUser());
        group.getAdministrators().add(getAuthenticatedUser());

        logger.info("Saving new group: " + group.getDescription());
        return groupRepository.save(group);
    }

    public Group update(Long id, String name, String description, MultipartFile file) throws GroupNotFoundException, UserNotFoundException {

        Group existGroup = findById(id);
        existGroup.setName(name);
        existGroup.setDescription(description);
        //existGroup.getAdministrators().add(getAuthenticatedUser());


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

    public Page<Group> findAll(String searchParam, Pageable pageable) {
        return groupRepository.findAll(searchParam, pageable);
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

    public long getTotal(){
        logger.info("Total groups: " + groupRepository.count());
        return groupRepository.count();
    }

    public Page<User> getMembersByGroupId(Long groupId, Pageable pageable) throws GroupNotFoundException {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException("Group not found with ID: " + groupId));
        return groupRepository.findMembersByGroupId(group.getId(), pageable);
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

        group.getAdministrators().remove(user);
        group.getMembers().remove(user);
        return groupRepository.save(group);
    }

    public boolean checkMembership(Long groupId, Long userId) {
        Group group = groupRepository.findById(groupId).orElse(null);
        User user = userRepository.findById(userId).orElse(null);

        if (group == null || user == null) {
            return false;
        }
        return group.getMembers().contains(user);
    }

    public long countMembersByGroupId(Long groupId){
        return groupRepository.countMembersByGroupId(groupId);
    }

    public Page<User> getAdministratorsByGroupId(Long groupId, Pageable pageable) throws GroupNotFoundException {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException("Group not found with ID: " + groupId));
        return groupRepository.findAdministratorsByGroupId(group.getId(), pageable);
    }

    public Group addMemberToGroupAdministrators(Long groupId, Long userId) throws GroupNotFoundException {
        Group group = findById(groupId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("No user found by id: " + userId));

        group.getAdministrators().add(user);
        return groupRepository.save(group);
    }

    public Group removeMemberFromGroupAdministrators(Long groupId, Long userId) throws GroupNotFoundException {
        Group group = findById(groupId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("No user found by id: " + userId));

        group.getAdministrators().remove(user);
        return groupRepository.save(group);
    }

    public boolean checkIsAdmin(Long groupId, Long userId) {
        Group group = groupRepository.findById(groupId).orElse(null);
        User user = userRepository.findById(userId).orElse(null);

        if (group == null || user == null) {
            return false;
        }
        return group.getAdministrators().contains(user);
    }

    public long countAdministratorsByGroupId(Long groupId){
        return groupRepository.countAdministratorsByGroupId(groupId);
    }

    public User getAuthenticatedUser() throws UserNotFoundException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User user = userRepository.findUserByUsername(username);
        if(user == null){
            throw new UserNotFoundException(NO_USER_FOUND_BY_USERNAME + username);
        }
        logger.info(FOUND_USER_BY_USERNAME + username);
        return userRepository.findUserByUsername(username);
    }

    private String generateGroupId() {
        return RandomStringUtils.randomAlphanumeric(10);
    }
}
