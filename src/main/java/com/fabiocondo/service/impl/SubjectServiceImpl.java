package com.fabiocondo.service.impl;

import com.fabiocondo.aws.model.S3UploadResponse;
import com.fabiocondo.aws.service.AmazonS3Service;
import com.fabiocondo.constant.CacheNames;
import com.fabiocondo.domain.*;
import com.fabiocondo.dto.PageResponse;
import com.fabiocondo.dto.SubjectDto;
import com.fabiocondo.dto.TopicContentDTO;
import com.fabiocondo.dto.TopicDTO;
import com.fabiocondo.enumeration.Category;
import com.fabiocondo.exception.domain.SubjectNotFoundException;
import com.fabiocondo.exception.domain.UserNotFoundException;
import com.fabiocondo.repository.SubjectRepository;
import com.fabiocondo.repository.TopicContentRepository;
import com.fabiocondo.repository.TopicRepository;
import com.fabiocondo.repository.UserRepository;
import com.fabiocondo.service.SubjectService;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;


@Service
public class SubjectServiceImpl implements SubjectService {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private static final String BUCKET_NAME = "dikahub-subject-bucket";

    private final AmazonS3Service amazonS3Service;
    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;
    private final TopicContentRepository contentRepository;
    private final TopicRepository topicRepository;
    private final TopicContentService topicContentService;


    public SubjectServiceImpl(AmazonS3Service amazonS3Service, SubjectRepository subjectRepository, UserRepository userRepository, TopicContentRepository contentRepository, TopicRepository topicRepository, TopicContentService topicContentService) {
        this.amazonS3Service = amazonS3Service;
        this.subjectRepository = subjectRepository;
        this.userRepository = userRepository;
        this.contentRepository = contentRepository;
        this.topicRepository = topicRepository;
        this.topicContentService = topicContentService;
    }

    @Override
    public Subject findById(Long id) throws SubjectNotFoundException {
        logger.info("Getting subject by id: " + id);
        return subjectRepository.findById(id)
                .orElseThrow(() -> new SubjectNotFoundException("No subject found by id: " + id));
    }

    public Subject findSubjectBySubjectId(String subjectId) throws SubjectNotFoundException {
        return subjectRepository.findSubjectBySubjectId(subjectId)
                .orElseThrow(() -> new SubjectNotFoundException("No subject found by id: " + subjectId));
    }

    @Override
    public Page<Subject> findByName(String name, boolean enabled, Pageable pageable) {
        return subjectRepository.findByNameAndCourseEnabled(name, enabled, pageable);
    }

    /*@Cacheable(
            value = CacheNames.SUBJECT_DETAIL,
            key = "#subjectId + '-' + #currentUserId",
            unless = "#result == null"
    )*/
    public SubjectDto findSubjectBySubjectIdWithCash(String subjectId, Long currentUserId)
            throws SubjectNotFoundException, UserNotFoundException {

        Subject subject = subjectRepository.findSubjectBySubjectId(subjectId)
                .orElseThrow(() -> new SubjectNotFoundException("Subject not found by id: " + subjectId));

        return domainToDto(subject, currentUserId);
    }

    @Cacheable(
            value = CacheNames.SUBJECT_FILTER,
            key =
                    "#enabled + '-' +" +
                            "#subjectId + '-' +" +
                            "#currentUserId + '-' +" +
                            "#pageable.pageNumber + '-' +" +
                            "#pageable.pageSize + '-' +" +
                            "#pageable.sort.toString()",
            unless = "#result == null"
    )
    public PageResponse<SubjectDto> findByNameWithCache(Long subjectId, boolean enabled, Long currentUserId, Pageable pageable) {

        Page<Subject> subjects = subjectRepository.findByIdAndCourseEnabled(subjectId, enabled, pageable);

        List<SubjectDto> content = subjects.getContent()
                .stream()
                .map(subject -> {
                    try {
                        return domainToDto(subject, currentUserId);
                    } catch (UserNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());

        return new PageResponse<>(
                content,
                subjects.getNumber(),
                subjects.getSize(),
                subjects.getTotalElements()
        );
    }

    @Override
    @Cacheable(value = CacheNames.SUBJECT_LIST, key = "'all'")
    public List<Subject> findAll() {
        logger.info("Getting subjects");
        return subjectRepository.findAll();
    }

    @CacheEvict(
            value = {
                    CacheNames.SUBJECT_LIST,
                    CacheNames.SUBJECT_FILTER,
                    CacheNames.SUBJECT_DETAIL,
            },
            allEntries = true
    )
    @Override
    public Subject save(String name, String description, Category category, boolean quizEnabled, boolean courseEnabled, boolean progressEnabled, boolean examEnabled, MultipartFile file) {
        logger.info("Uploading file: " + file.getOriginalFilename());
        //String fileKey = UUID.randomUUID() + "-" + file.getOriginalFilename();
        String fileKey = "subject_image_profile_" + RandomStringUtils.randomNumeric(4).toLowerCase() + "_" + file.getOriginalFilename();
        S3UploadResponse s3UploadResponse = amazonS3Service.uploadFile(file, BUCKET_NAME, fileKey);

        Subject subject = new Subject();
        subject.setSubjectId(UUID.randomUUID().toString());
        subject.setName(name);
        subject.setDescription(description);
        subject.setCategory(category);
        subject.setQuizEnabled(quizEnabled);
        subject.setCourseEnabled(courseEnabled);
        subject.setProgressEnabled(progressEnabled);
        subject.setExamEnabled(examEnabled);
        subject.setUrlFile(s3UploadResponse.getFileUrl());
        subject.setFileName(fileKey);

        logger.info("Saving new subject: " + subject.getName());
        return subjectRepository.save(subject);
    }

    @CacheEvict(
            value = {
                    CacheNames.SUBJECT_LIST,
                    CacheNames.SUBJECT_FILTER,
                    CacheNames.SUBJECT_DETAIL,
            },
            allEntries = true
    )
    @Override
    public Subject update(Long id, String name, String description, Category category, boolean quizEnabled, boolean courseEnabled, boolean progressEnabled, boolean examEnabled, MultipartFile file) throws SubjectNotFoundException {
        Subject existSubject = findById(id);
        existSubject.setName(name);
        existSubject.setDescription(description);
        existSubject.setCategory(category);
        existSubject.setQuizEnabled(quizEnabled);
        existSubject.setCourseEnabled(courseEnabled);
        existSubject.setProgressEnabled(progressEnabled);
        existSubject.setExamEnabled(examEnabled);

        // Se um novo arquivo é fornecido, atualiza o arquivo no serviço Amazon S3 e atualiza o nome e a URL do arquivo
        if (file != null) {
            if (existSubject.getFileName() != null) {
                logger.info("Deleting file: " + existSubject.getFileName());
                amazonS3Service.deleteFile(existSubject.getFileName(), BUCKET_NAME);
            }
            //String newFileKey = UUID.randomUUID() + "-" + file.getOriginalFilename();
            String newFileKey = "subject_image_profile_" + RandomStringUtils.randomNumeric(4).toLowerCase() + "_" + file.getOriginalFilename();
            S3UploadResponse s3UploadResponse = amazonS3Service.uploadFile(file, BUCKET_NAME, newFileKey);
            existSubject.setUrlFile(s3UploadResponse.getFileUrl());
            existSubject.setFileName(newFileKey);
        }

        logger.info("Updating subject: " + existSubject.getName());
        return subjectRepository.save(existSubject);
    }

    @CacheEvict(
            value = {
                    CacheNames.SUBJECT_LIST,
                    CacheNames.SUBJECT_FILTER,
                    CacheNames.SUBJECT_DETAIL,
            },
            allEntries = true
    )
    public void delete(Long id) throws SubjectNotFoundException {
        Subject existSubject = findById(id);
        if (existSubject.getFileName() != null) {
            logger.info("Deleting file: " + existSubject.getFileName());
            amazonS3Service.deleteFile(existSubject.getFileName(), BUCKET_NAME);
        }

        logger.info("Deleting subject: " + existSubject.getName());
        subjectRepository.deleteById(id);
    }

    public double calculateUserProgressInSubject(Long userId, Long subjectId) throws UserNotFoundException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("No user found by id: " + userId));

        List<TopicContent> contents = contentRepository.findByTopic_Subject_Id(subjectId);

        long totalMarked = user.getMarkedTopicContents().stream()
                .filter(contents::contains)
                .count();

        if (contents.isEmpty()) return 0;
        return (double) totalMarked / contents.size() * 100;
    }

    public SubjectDto domainToDto(Subject subject, Long currentUserId) throws UserNotFoundException {

        SubjectDto subjectDto = new SubjectDto();
        subjectDto.setId(subject.getId());
        subjectDto.setSubjectId(subject.getSubjectId());
        subjectDto.setName(subject.getName());
        subjectDto.setDescription(subject.getDescription());
        subjectDto.setCategory(subject.getCategory());
        subjectDto.setFileName(subject.getFileName());
        subjectDto.setUrlFile(subject.getUrlFile());

        // Campos calculados
        subjectDto.setTotalTopics(topicRepository.countBySubjectIdAndEnabledTrue(subject.getId()));
        subjectDto.setTotalLessons(topicContentService.getTotalVideoLessons(subject.getId()));
        subjectDto.setTotalFiles(topicContentService.getTotalFiles(subject.getId()));

        subjectDto.setExamEnabled(subject.isExamEnabled());
        subjectDto.setCourseEnabled(subject.isCourseEnabled());
        subjectDto.setProgressEnabled(subject.isProgressEnabled());
        subjectDto.setQuizEnabled(subject.isQuizEnabled());

        // Buscar conteúdos marcados do usuário
        Set<Long> markedContentIds = new HashSet<>();

        Optional<User> currentUser = userRepository.findById(currentUserId);

        if (currentUser.isPresent()) {
            markedContentIds = currentUser.get()
                    .getMarkedTopicContents()
                    .stream()
                    .map(TopicContent::getId)
                    .collect(Collectors.toSet());

            subjectDto.setCurrentUserMarkedContentRate(calculateUserProgressInSubject(currentUserId, subject.getId())
            );
        }

        // Converter topics
        if (subject.getTopics() != null && !subject.getTopics().isEmpty()) {

            Set<Long> finalMarkedContentIds = markedContentIds;
            List<TopicDTO> topicDTOs = subject.getTopics()
                    .stream()
                    .filter(Topic::isEnabled)
                    .map(topic -> {

                        TopicDTO topicDTO = new TopicDTO();
                        topicDTO.setId(topic.getId());
                        topicDTO.setTopicId(topic.getTopicId());
                        topicDTO.setName(topic.getName());
                        topicDTO.setDescription(topic.getDescription());
                        topicDTO.setPosition(topic.getPosition());
                        topicDTO.setPremium(topic.isPremium());
                        topicDTO.setEnabled(topic.isEnabled());

                        // Converter contents
                        List<TopicContentDTO> contentDTOs = topic.getContents()
                                .stream()
                                .map(content -> {

                                    TopicContentDTO contentDTO =
                                            new TopicContentDTO();

                                    contentDTO.setId(content.getId());
                                    contentDTO.setDescription(
                                            content.getDescription()
                                    );
                                    contentDTO.setUrlFile(
                                            content.getUrlFile()
                                    );
                                    contentDTO.setFileName(
                                            content.getFileName()
                                    );
                                    contentDTO.setPosition(
                                            content.getPosition()
                                    );
                                    contentDTO.setTime(
                                            content.getTime()
                                    );
                                    contentDTO.setContentType(
                                            content.getContentType()
                                    );

                                    // AQUI está o setMarkedByUser correto
                                    contentDTO.setMarkedByUser(
                                            finalMarkedContentIds.contains(
                                                    content.getId()
                                            )
                                    );

                                    return contentDTO;
                                })
                                .sorted(
                                        Comparator.comparing(
                                                TopicContentDTO::getPosition
                                        )
                                )
                                .collect(Collectors.toList());

                        topicDTO.setContents(contentDTOs);

                        return topicDTO;
                    })
                    .sorted(Comparator.comparing(TopicDTO::getPosition))
                    .collect(Collectors.toList());

            subjectDto.setTopics(topicDTOs);
        }

        return subjectDto;
    }
}
