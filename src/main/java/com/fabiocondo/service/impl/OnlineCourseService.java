package com.fabiocondo.service.impl;

import com.fabiocondo.aws.model.S3UploadResponse;
import com.fabiocondo.aws.service.AmazonS3Service;
import com.fabiocondo.domain.OnlineCourse;
import com.fabiocondo.domain.OnlineCourseContent;
import com.fabiocondo.domain.User;
import com.fabiocondo.exception.domain.OnlineCourseNotFoundException;
import com.fabiocondo.exception.domain.UserNotFoundException;
import com.fabiocondo.repository.OnlineCourseContentRepository;
import com.fabiocondo.repository.OnlineCourseRepository;
import com.fabiocondo.repository.UserRepository;
import com.fabiocondo.repository.filter.OnlineCourseFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
public class OnlineCourseService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final String BUCKET_NAME = "b-tests-bucket";

    private final OnlineCourseRepository onlineCourseRepository;

    private final OnlineCourseContentRepository onlineCourseContentRepository;

    private final UserRepository userRepository;

    private final UserServiceImpl userService;

    private final AmazonS3Service amazonS3Service;

    public OnlineCourseService(OnlineCourseRepository onlineCourseRepository, OnlineCourseContentRepository onlineCourseContentRepository, UserRepository userRepository, UserServiceImpl userService, AmazonS3Service amazonS3Service) {
        this.onlineCourseRepository = onlineCourseRepository;
        this.onlineCourseContentRepository = onlineCourseContentRepository;
        this.userRepository = userRepository;
        this.userService = userService;
        this.amazonS3Service = amazonS3Service;
    }

    public OnlineCourse findById(Long id) throws OnlineCourseNotFoundException {
        logger.info("Getting course by id: " + id);
        return onlineCourseRepository.findById(id)
                .orElseThrow(() -> new OnlineCourseNotFoundException("No course found by id: " + id));
    }

    public OnlineCourse findOnlineCourseByOnlineCourseId(String onlineCourseId) throws OnlineCourseNotFoundException {
        return onlineCourseRepository.findOnlineCourseByOnlineCourseId(onlineCourseId)
                .orElseThrow(() -> new OnlineCourseNotFoundException("No course found by id: " + onlineCourseId));
    }

    public Page<OnlineCourse> findAll(String searchParam, Pageable pageable) {
        return onlineCourseRepository.findAll(searchParam, pageable);
    }

    public Page<OnlineCourse> filter(OnlineCourseFilter onlineCourseFilter, Pageable pageable) {
        return onlineCourseRepository.filter(onlineCourseFilter, pageable);
    }

    public OnlineCourse save(String name, String description, String lunchDate, Long instrutorId, MultipartFile file) throws UserNotFoundException {
        logger.info("Uploading file: " + file.getOriginalFilename());
        S3UploadResponse s3UploadResponse = amazonS3Service.uploadFile(file, BUCKET_NAME);

        User instrutor = userService.findById(instrutorId);

        OnlineCourse course = new OnlineCourse();
        course.setName(name);
        course.setOnlineCourseId(UUID.randomUUID().toString());
        course.setDescription(description);
        course.setLunchDate(lunchDate);
        course.setInstrutor(instrutor);
        course.setCoverImageUrl(s3UploadResponse.getFileUrl());
        course.setFileName(file.getOriginalFilename());

        logger.info("Saving new course: " + course.getDescription());
        return onlineCourseRepository.save(course);
    }

    public OnlineCourse update(Long id, String name, String description, String lunchDate, Long instrutorId, MultipartFile file) throws UserNotFoundException, OnlineCourseNotFoundException {
        User instrutor = userService.findById(instrutorId);

        OnlineCourse existCourse = findById(id);
        existCourse.setName(name);
        existCourse.setDescription(description);
        existCourse.setLunchDate(lunchDate);
        existCourse.setInstrutor(instrutor);

        // Se um novo arquivo é fornecido, atualiza o arquivo no serviço Amazon S3 e atualiza o nome e a URL do arquivo
        if (file != null) {
            if (existCourse.getFileName() != null) {
                logger.info("Deleting file: " + existCourse.getFileName());
                amazonS3Service.deleteFile(existCourse.getFileName(), BUCKET_NAME);
            }
            S3UploadResponse s3UploadResponse = amazonS3Service.uploadFile(file, BUCKET_NAME);
            existCourse.setCoverImageUrl(s3UploadResponse.getFileUrl());
            existCourse.setFileName(file.getOriginalFilename());
        }

        logger.info("Saving new course: " + existCourse.getDescription());
        return onlineCourseRepository.save(existCourse);
    }

    public void delete(Long id) throws OnlineCourseNotFoundException {
        OnlineCourse existCourse = findById(id);
        logger.info("Deleting course: " + existCourse.getDescription());
        onlineCourseRepository.deleteById(id);
        if (existCourse.getFileName() != null) {
            logger.info("Deleting file: " + existCourse.getFileName());
            amazonS3Service.deleteFile(existCourse.getFileName(), BUCKET_NAME);
        }
    }

    public long getTotal(){
        logger.info("Total course: " + onlineCourseRepository.count());
        return onlineCourseRepository.count();
    }

    public Page<User> getStudentsByCourseId(Long courseId, Pageable pageable) throws OnlineCourseNotFoundException {
        OnlineCourse course = findById(courseId);
        return onlineCourseRepository.findStudentsByCourseId(course.getId(), pageable);
    }

    public double calculateUserProgressInCourse(Long userId, Long courseId) throws UserNotFoundException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("No user found by id: " + userId));

        List<OnlineCourseContent> contents = onlineCourseContentRepository.findByModule_OnlineCourse_Id(courseId);

        long totalMarked = user.getMarkedCourseContents().stream()
                .filter(contents::contains)
                .count();

        if (contents.isEmpty()) return 0;
        return (double) totalMarked / contents.size() * 100;
    }

}
