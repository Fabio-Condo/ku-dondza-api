package com.fabiocondo.service.impl;

import com.fabiocondo.aws.model.S3UploadResponse;
import com.fabiocondo.aws.service.AmazonS3Service;
import com.fabiocondo.domain.Subject;
import com.fabiocondo.domain.Teacher;
import com.fabiocondo.exception.domain.CombinationExistException;
import com.fabiocondo.exception.domain.SubjectNotFoundException;
import com.fabiocondo.exception.domain.TeacherNotFoundException;
import com.fabiocondo.repository.TeacherRepository;
import com.fabiocondo.service.TeacherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;
import java.util.function.Predicate;

@Service
public class TeacherServiceImpl implements TeacherService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final String BUCKET_NAME = "b-tests-bucket";

    public TeacherRepository teacherRepository;

    public SubjectServiceImpl subjectServiceImpl;

    private final AmazonS3Service amazonS3Service;

    public TeacherServiceImpl(TeacherRepository teacherRepository, SubjectServiceImpl subjectServiceImpl, AmazonS3Service amazonS3Service) {
        this.teacherRepository = teacherRepository;
        this.subjectServiceImpl = subjectServiceImpl;
        this.amazonS3Service = amazonS3Service;
    }

    @Override
    public Teacher findById(Long id) throws TeacherNotFoundException {
        logger.info("Getting course by id: " + id);
        return teacherRepository.findById(id)
                .orElseThrow(() -> new TeacherNotFoundException("No teacher found by id: " + id));
    }

    @Override
    public Page<Teacher> findAll(Pageable pageable) {
        return teacherRepository.findAll(pageable);
    }

    @Override
    public Page<Teacher> findByName(String name, Pageable pageable) {
        return teacherRepository.findByName(name, pageable);
    }

    @Override
    public Teacher save(String name, String email, String contactNumber, MultipartFile file) {
        logger.info("Uploading file: " + file.getOriginalFilename());
        S3UploadResponse s3UploadResponse = amazonS3Service.uploadFile(file, BUCKET_NAME);

        Teacher teacher = new Teacher();
        teacher.setName(name);
        teacher.setEmail(email);
        teacher.setContactNumber(contactNumber);
        teacher.setUrlFile(s3UploadResponse.getFileUrl());
        teacher.setFileName(file.getOriginalFilename());

        logger.info("Saving new teacher: " + teacher.getName());
        return teacherRepository.save(teacher);
    }

    @Override
    public Teacher update(Long id, String name, String email, String contactNumber, MultipartFile file) throws TeacherNotFoundException {
        Teacher existTeacher = findById(id);
        existTeacher.setName(name);
        existTeacher.setEmail(email);
        existTeacher.setContactNumber(contactNumber);

        // Se um novo arquivo é fornecido, atualiza o arquivo no serviço Amazon S3 e atualiza o nome e a URL do arquivo
        if (file != null) {
            if (existTeacher.getFileName() != null) {
                logger.info("Deleting file: " + existTeacher.getFileName());
                amazonS3Service.deleteFile(existTeacher.getFileName(), BUCKET_NAME);
            }
            S3UploadResponse s3UploadResponse = amazonS3Service.uploadFile(file, BUCKET_NAME);
            existTeacher.setUrlFile(s3UploadResponse.getFileUrl());
            existTeacher.setFileName(file.getOriginalFilename());
        }

        logger.info("Saving new teacher: " + existTeacher.getName());
        return teacherRepository.save(existTeacher);
    }

    @Override
    public void delete(Long id) throws TeacherNotFoundException {
        Teacher existTeacher = findById(id);
        logger.info("Deleting exame: " + existTeacher.getName());
        teacherRepository.deleteById(id);
        if (existTeacher.getFileName() != null) {
            logger.info("Deleting file: " + existTeacher.getFileName());
            amazonS3Service.deleteFile(existTeacher.getFileName(), BUCKET_NAME);
        }
    }

    @Override
    public Teacher addSubjectToTeacherSubjectsList(Long teacherId, Long subjectId) throws TeacherNotFoundException, SubjectNotFoundException, CombinationExistException {
        Teacher teacher = findById(teacherId);
        Subject subject = subjectServiceImpl.findById(subjectId);

        if(!teacher.getSubjects().isEmpty()) {

            Predicate<Subject> subjectPredicate = subj -> subj.getId().equals(subjectId);
            Optional<Subject> optionalSubject = teacher.getSubjects().stream()
                    .filter(subjectPredicate)
                    .findFirst();

            if(optionalSubject.isPresent()) {
                throw new CombinationExistException("Subject already exist on this teacher subjects list");
            }
        }

        teacher.addSubjectToTeacherSubjectsList(subject);
        return teacherRepository.save(teacher);
    }

    @Override
    public Teacher removeSubjectFromTeacherSubjectsList(Long teacherId, Long subjectId) throws TeacherNotFoundException, SubjectNotFoundException {
        Teacher teacher = findById(teacherId);
        Subject subject = subjectServiceImpl.findById(subjectId);
        teacher.removeSubjectFromTeacherSubjectsList(subject);
        return teacherRepository.save(teacher);
    }

    @Override
    public long getTotal(){
        logger.info("Total teacher: " + teacherRepository.count());
        return teacherRepository.count();
    }

}
