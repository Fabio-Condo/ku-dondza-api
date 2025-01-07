package com.fabiocondo.service.impl;

import com.fabiocondo.aws.model.S3UploadResponse;
import com.fabiocondo.aws.service.AmazonS3Service;
import com.fabiocondo.domain.Question;
import com.fabiocondo.exception.domain.QuestionNotFoundException;
import com.fabiocondo.repository.QuestionRepository;
import com.fabiocondo.repository.filter.QuestionFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class QuestionService {

    private static final String BUCKET_NAME = "b-tests-bucket";
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final AmazonS3Service amazonS3Service;
    private final QuestionRepository questionRepository;

    public QuestionService(AmazonS3Service amazonS3Service, QuestionRepository questionRepository) {
        this.amazonS3Service = amazonS3Service;
        this.questionRepository = questionRepository;
    }

    public Question findById(Long id) throws QuestionNotFoundException {
        logger.info("Getting question by id: " + id);
        return questionRepository.findById(id)
                .orElseThrow(() -> new QuestionNotFoundException("No question found by id: " + id));
    }

    public Page<Question> filter(QuestionFilter questionFilter, Pageable pageable) {
        return questionRepository.filter(questionFilter, pageable);
    }

    public Page<Question> findAll(String searchParam, Pageable pageable) {
        return questionRepository.findAll(searchParam, pageable);
    }

    public List<Question> findAll() {
        return questionRepository.findAll();
    }

    public Question save(Question question) {
        question.getAnswers().forEach(answer -> answer.setQuestion(question));
        logger.info("Saving question: " + question.getText());
        return questionRepository.save(question);
    }

    public Question update(Question question, Long id) throws QuestionNotFoundException {
        Question existQuestion = findById(id);

        existQuestion.getAnswers().clear();
        existQuestion.getAnswers().addAll(question.getAnswers());
        existQuestion.getAnswers().forEach(answer -> answer.setQuestion(existQuestion));

        BeanUtils.copyProperties(question, existQuestion, "answers");
        logger.info("Updating question: " + existQuestion.getText());
        return questionRepository.save(existQuestion);
    }

    public void delete(Long id) throws QuestionNotFoundException {
        Question existQuestion = findById(id);
        logger.info("Deleting quiz: " + existQuestion.getText());
        questionRepository.deleteById(id);
    }

    public long getTotal(){
        logger.info("Total quizzes: " + questionRepository.count());
        return questionRepository.count();
    }

    public Question updateQuestionImage(Long questionId, MultipartFile file) throws QuestionNotFoundException, IOException {
        // Adicionar funcao que diminue o tamanho da imagem

        Question question = findById(questionId);

        if (file == null || file.isEmpty()) {
            throw new IOException("The file is null or empty");
        }

        // Deleta o arquivo antigo do S3
        if (question.getFileName() != null) {
            logger.info("Deleting file: " + question.getFileName());
            amazonS3Service.deleteFile(question.getFileName(), BUCKET_NAME);
        }

        S3UploadResponse s3UploadResponse = amazonS3Service.uploadFile(file, BUCKET_NAME);
        question.setUrlFile(s3UploadResponse.getFileUrl());
        question.setFileName(file.getOriginalFilename());

        questionRepository.save(question);
        return question;
    }
}
