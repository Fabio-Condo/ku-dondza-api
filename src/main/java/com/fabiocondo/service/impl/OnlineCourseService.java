package com.fabiocondo.service.impl;

import com.fabiocondo.aws.model.S3UploadResponse;
import com.fabiocondo.aws.service.AmazonS3Service;
import com.fabiocondo.domain.OnlineCourse;
import com.fabiocondo.domain.Question;
import com.fabiocondo.domain.User;
import com.fabiocondo.exception.domain.CourseNotFoundException;
import com.fabiocondo.exception.domain.QuestionNotFoundException;
import com.fabiocondo.exception.domain.UserNotFoundException;
import com.fabiocondo.repository.OnlineCourseRepository;
import com.fabiocondo.repository.QuestionRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class OnlineCourseService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final String BUCKET_NAME = "b-tests-bucket";

    private final OnlineCourseRepository onlineCourseRepository;

    private final QuestionRepository questionRepository;

    private final UserServiceImpl userService;

    private final AmazonS3Service amazonS3Service;


    public OnlineCourseService(OnlineCourseRepository onlineCourseRepository, QuestionRepository questionRepository, UserServiceImpl userService, AmazonS3Service amazonS3Service) {
        this.onlineCourseRepository = onlineCourseRepository;
        this.questionRepository = questionRepository;
        this.userService = userService;
        this.amazonS3Service = amazonS3Service;
    }

    public OnlineCourse findById(Long id) throws CourseNotFoundException {
        logger.info("Getting course by id: " + id);
        return onlineCourseRepository.findById(id)
                .orElseThrow(() -> new CourseNotFoundException("No course found by id: " + id));
    }

    public OnlineCourse findOnlineCourseByOnlineCourseId(String onlineCourseId) throws CourseNotFoundException {
        return onlineCourseRepository.findOnlineCourseByOnlineCourseId(onlineCourseId)
                .orElseThrow(() -> new CourseNotFoundException("No course found by id: " + onlineCourseId));
    }

    public Page<OnlineCourse> findAll(String searchParam, Pageable pageable) {
        return onlineCourseRepository.findAll(searchParam, pageable);
    }

    public OnlineCourse save(String name, String description, String requirements, String lunchDate, Long instrutorId, MultipartFile file) throws UserNotFoundException {
        logger.info("Uploading file: " + file.getOriginalFilename());
        S3UploadResponse s3UploadResponse = amazonS3Service.uploadFile(file, BUCKET_NAME);

        User instrutor = userService.findById(instrutorId);

        OnlineCourse course = new OnlineCourse();
        course.setName(name);
        course.setOnlineCourseId(generateOnlineCourseId());
        course.setDescription(description);
        course.setRequirements(requirements);
        course.setLunchDate(lunchDate);
        course.setInstrutor(instrutor);
        course.setCoverImageUrl(s3UploadResponse.getFileUrl());
        course.setFileName(file.getOriginalFilename());

        logger.info("Saving new course: " + course.getDescription());
        return onlineCourseRepository.save(course);
    }

    public OnlineCourse update(Long id, String name, String description, String requirements, String lunchDate, Long instrutorId, MultipartFile file) throws CourseNotFoundException, UserNotFoundException {
        User instrutor = userService.findById(instrutorId);

        OnlineCourse existCourse = findById(id);
        existCourse.setName(name);
        existCourse.setDescription(description);
        existCourse.setRequirements(requirements);
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

    public void delete(Long id) throws CourseNotFoundException {
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

    public Page<User> getStudentsByCourseId(Long courseId, Pageable pageable) throws CourseNotFoundException {
        OnlineCourse existCourse = findById(courseId);
        return onlineCourseRepository.findStudentsByCourseId(existCourse.getId(), pageable);
    }

    public long countOnlineCourseStudentsByCourseId(Long courseId){
        return onlineCourseRepository.countOnlineCourseStudentsByCourseId(courseId);
    }

    public Page<Question> getQuestionsByCourseId(Long courseId, Pageable pageable) throws CourseNotFoundException {
        OnlineCourse course = findById(courseId);
        return onlineCourseRepository.findQuestionsByCourseId(course.getId(), pageable);
    }

    public OnlineCourse addQuestionToCourse(Long courseId, Long questionId) throws CourseNotFoundException, QuestionNotFoundException {
        OnlineCourse course = findById(courseId);
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new QuestionNotFoundException("No question found by id: " + questionId));
        course.getQuestions().add(question);
        return onlineCourseRepository.save(course);
    }

    public OnlineCourse removeQuestionFromCourse(Long courseId, Long questionId) throws QuestionNotFoundException, CourseNotFoundException {
        OnlineCourse course = findById(courseId);
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new QuestionNotFoundException("No question found by id: " + questionId));
        course.getQuestions().remove(question);
        return onlineCourseRepository.save(course);
    }

    public long countQuestionsByCourseId(Long courseId){
        return onlineCourseRepository.countQuestionsByCourseId(courseId);
    }

    private String generateOnlineCourseId() {
        return RandomStringUtils.randomAlphanumeric(10);
    }
}
