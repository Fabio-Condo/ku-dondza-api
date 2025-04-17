package com.fabiocondo.service.impl;

import com.fabiocondo.aws.model.S3UploadResponse;
import com.fabiocondo.aws.service.AmazonS3Service;
import com.fabiocondo.domain.Course;
import com.fabiocondo.domain.Module;
import com.fabiocondo.domain.Content;
import com.fabiocondo.domain.User;
import com.fabiocondo.exception.domain.OnlineCourseNotFoundException;
import com.fabiocondo.exception.domain.UserNotFoundException;
import com.fabiocondo.repository.ContentRepository;
import com.fabiocondo.repository.CourseRepository;
import com.fabiocondo.repository.UserRepository;
import com.fabiocondo.repository.filter.CourseFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.fabiocondo.constant.UserImplConstant.NO_USER_FOUND_BY_USERNAME;

@Service
public class CourseService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final String BUCKET_NAME = "b-tests-bucket";

    private final CourseRepository courseRepository;

    private final ContentRepository contentRepository;

    private final UserRepository userRepository;

    private final UserServiceImpl userService;

    private final AmazonS3Service amazonS3Service;

    public CourseService(CourseRepository courseRepository, ContentRepository contentRepository, UserRepository userRepository, UserServiceImpl userService, AmazonS3Service amazonS3Service) {
        this.courseRepository = courseRepository;
        this.contentRepository = contentRepository;
        this.userRepository = userRepository;
        this.userService = userService;
        this.amazonS3Service = amazonS3Service;
    }

    public Course findById(Long id) throws OnlineCourseNotFoundException {
        logger.info("Getting course by id: " + id);
        return courseRepository.findById(id)
                .orElseThrow(() -> new OnlineCourseNotFoundException("No course found by id: " + id));
    }

    public Course findOnlineCourseByOnlineCourseId(String onlineCourseId) throws OnlineCourseNotFoundException, UserNotFoundException {
        Course course = courseRepository.findOnlineCourseByOnlineCourseId(onlineCourseId)
                .orElseThrow(() -> new OnlineCourseNotFoundException("No course found by id: " + onlineCourseId));

        User user = getAuthenticatedUser();

        Set<Long> markedIds = user.getMarkedContents().stream()
                .map(Content::getId)
                .collect(Collectors.toSet());

        for (Module module : course.getModules()) {
            for (Content content : module.getCourseContents()) {
                content.setMarkedByUser(markedIds.contains(content.getId()));
            }
        }
        return course;
    }

    public Page<Course> findAll(String searchParam, Pageable pageable) {
        return courseRepository.findAll(searchParam, pageable);
    }

    public Page<Course> filter(CourseFilter courseFilter, Pageable pageable) {
        return courseRepository.filter(courseFilter, pageable);
    }

    public Course save(String name, String description, String lunchDate, Long instrutorId, MultipartFile file) throws UserNotFoundException {
        logger.info("Uploading file: " + file.getOriginalFilename());
        S3UploadResponse s3UploadResponse = amazonS3Service.uploadFile(file, BUCKET_NAME);

        User instrutor = userService.findById(instrutorId);

        Course course = new Course();
        course.setName(name);
        course.setOnlineCourseId(UUID.randomUUID().toString());
        course.setDescription(description);
        course.setLunchDate(lunchDate);
        course.setInstrutor(instrutor);
        course.setCoverImageUrl(s3UploadResponse.getFileUrl());
        course.setFileName(file.getOriginalFilename());

        logger.info("Saving new course: " + course.getDescription());
        return courseRepository.save(course);
    }

    public Course update(Long id, String name, String description, String lunchDate, Long instrutorId, MultipartFile file) throws UserNotFoundException, OnlineCourseNotFoundException {
        User instrutor = userService.findById(instrutorId);

        Course existCourse = findById(id);
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
        return courseRepository.save(existCourse);
    }

    public void delete(Long id) throws OnlineCourseNotFoundException {
        Course existCourse = findById(id);
        logger.info("Deleting course: " + existCourse.getDescription());
        courseRepository.deleteById(id);
        if (existCourse.getFileName() != null) {
            logger.info("Deleting file: " + existCourse.getFileName());
            amazonS3Service.deleteFile(existCourse.getFileName(), BUCKET_NAME);
        }
    }

    public long getTotal(){
        logger.info("Total course: " + courseRepository.count());
        return courseRepository.count();
    }

    public boolean checkIfCurrentUserSubscribed(Long onlineCourseId) throws UserNotFoundException {
        User user = getAuthenticatedUser();
        if (user == null) {
            return false;
        }
        Optional<Course> course = courseRepository.findById(onlineCourseId);
        if (!course.isPresent()) {
            return false;
        }
        return user.getSubscribedCourses().contains(course.get());
    }

    public Page<User> getStudentsByCourseId(Long courseId, Pageable pageable) throws OnlineCourseNotFoundException {
        Course course = findById(courseId);
        return courseRepository.findStudentsByCourseId(course.getId(), pageable);
    }

    public double calculateUserProgressInCourse(Long userId, Long courseId) throws UserNotFoundException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("No user found by id: " + userId));

        List<Content> contents = contentRepository.findByModule_Course_Id(courseId);

        long totalMarked = user.getMarkedContents().stream()
                .filter(contents::contains)
                .count();

        if (contents.isEmpty()) return 0;
        return (double) totalMarked / contents.size() * 100;
    }

    public User getAuthenticatedUser() throws UserNotFoundException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User user = userRepository.findUserByEmail(username);
        if(user == null){
            throw new UserNotFoundException(NO_USER_FOUND_BY_USERNAME + username);
        }
        return userRepository.findUserByEmail(username);
    }

}
