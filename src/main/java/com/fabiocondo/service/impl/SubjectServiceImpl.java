package com.fabiocondo.service.impl;

import com.fabiocondo.domain.*;
import com.fabiocondo.exception.domain.SubjectNotFoundException;
import com.fabiocondo.exception.domain.UserNotFoundException;
import com.fabiocondo.repository.SubjectRepository;
import com.fabiocondo.repository.TopicContentRepository;
import com.fabiocondo.repository.UserRepository;
import com.fabiocondo.service.SubjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
public class SubjectServiceImpl implements SubjectService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    SubjectRepository subjectRepository;

    UserRepository userRepository;

    private final TopicContentRepository contentRepository;

    public SubjectServiceImpl(SubjectRepository subjectRepository, UserRepository userRepository, TopicContentRepository contentRepository) {
        this.subjectRepository = subjectRepository;
        this.userRepository = userRepository;
        this.contentRepository = contentRepository;
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

    public Subject findSubjectBySubjectId(String onlineCourseId, Long currentUserId)
            throws SubjectNotFoundException {

        Subject subject = subjectRepository.findSubjectBySubjectId(onlineCourseId)
                .orElseThrow(() -> new SubjectNotFoundException("No subject found by id: " + onlineCourseId));

        // Tenta buscar o usuário, mas continua normalmente se não existir
        Optional<User> optionalUser = userRepository.findById(currentUserId);

        if (optionalUser.isPresent()) {
            User currentUser = optionalUser.get();

            Set<Long> markedIds = currentUser.getMarkedTopicContents().stream()
                    .map(TopicContent::getId)
                    .collect(Collectors.toSet());

            for (Topic topic : subject.getTopics()) {
                for (TopicContent content : topic.getContents()) {
                    content.setMarkedByUser(markedIds.contains(content.getId()));
                }
            }
        }

        return subject;
    }

    public boolean checkIfCurrentUserSubscribed(Long subjectId, Long currentUserId) {
        User currentUser = userRepository.findById(currentUserId).orElseThrow(null);
        if (currentUser == null) {
            return false;
        }
        Optional<Subject> subject = subjectRepository.findById(subjectId);
        if (!subject.isPresent()) {
            return false;
        }
        return currentUser.getSubscribedSubjects().contains(subject.get());
    }

    public Page<User> getStudentsByCourseId(Long courseId, Pageable pageable) throws SubjectNotFoundException {
        Subject subject = findById(courseId);
        return subjectRepository.findStudentsBySubjectId(subject.getId(), pageable);
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

    @Override
    public List<Subject> findAll() {
        return subjectRepository.findAll();
    }

    @Override
    public Page<Subject> findAll(Pageable pageable) {
        return subjectRepository.findAll(pageable);
    }

    @Override
    public Page<Subject> findByName(String name, Pageable pageable) {
        return subjectRepository.findByName(name, pageable);
    }

    @Override
    public Subject save(Subject subject) {
        subject.setSubjectId(UUID.randomUUID().toString());
        return subjectRepository.save(subject);
    }

    @Override
    public Subject update(Subject subject, Long id) throws SubjectNotFoundException {
        Subject existSubject = findById(id);
        BeanUtils.copyProperties(subject, existSubject, "id", "subjectId", "topics", "students");
        logger.info("Updating subject: " + subject.getName());
        return subjectRepository.save(existSubject);
    }

    public void delete(Long id) throws SubjectNotFoundException {
        Subject existSubject = findById(id);
        logger.info("Deleting subject: " + existSubject.getName());
        subjectRepository.deleteById(id);
    }

    public long getTotal(){
        return subjectRepository.count();
    }
}
