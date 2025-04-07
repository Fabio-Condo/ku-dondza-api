package com.fabiocondo.service.impl;

import com.fabiocondo.aws.model.S3UploadResponse;
import com.fabiocondo.aws.service.AmazonS3Service;
import com.fabiocondo.domain.Book;
import com.fabiocondo.domain.Subject;
import com.fabiocondo.exception.domain.BookNotFoundException;
import com.fabiocondo.exception.domain.SubjectNotFoundException;
import com.fabiocondo.repository.BookRepository;
import com.fabiocondo.repository.filter.BookFilter;
import com.fabiocondo.service.BookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class BookServiceImpl implements BookService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final String BUCKET_NAME = "b-tests-bucket";

    public BookRepository bookRepository;

    private final AmazonS3Service amazonS3Service;

    private final SubjectServiceImpl subjectServiceImpl;

    @Autowired
    public BookServiceImpl(BookRepository bookRepository, AmazonS3Service amazonS3Service, SubjectServiceImpl subjectServiceImpl) {
        this.bookRepository = bookRepository;
        this.amazonS3Service = amazonS3Service;
        this.subjectServiceImpl = subjectServiceImpl;
    }

    @Override
    public Book findById(Long id) throws BookNotFoundException {
        logger.info("Getting book by id: " + id);
        return bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException("No book found by id: " + id));
    }

    @Override
    public Page<Book> filter(BookFilter bookFilter, Pageable pageable) {
        return bookRepository.filter(bookFilter, pageable);
    }

    @Override
    public Page<Book> findAll(Pageable pageable) {
        return bookRepository.findAll(pageable);
    }

    @Override
    public List<Book> findAll() {
        return bookRepository.findAll();
    }

    @Override
    public Book save(String name, String description, String author, Long subjectId, MultipartFile coverImageFile, MultipartFile file) throws SubjectNotFoundException {
        logger.info("Uploading file: " + file.getOriginalFilename());
        S3UploadResponse s3UploadResponse = amazonS3Service.uploadFile(file, BUCKET_NAME);
        S3UploadResponse s3UploadResponseCoverImageFile = amazonS3Service.uploadFile(coverImageFile, BUCKET_NAME);

        Subject subject = subjectServiceImpl.findById(subjectId);

        Book book = new Book();
        book.setSubject(subject);
        book.setName(name);
        book.setDescription(description);
        book.setAuthor(author);
        book.setTotalDownloadNumber(0L);
        book.setUrlFile(s3UploadResponse.getFileUrl());
        book.setFileName(file.getOriginalFilename());
        book.setUrlCoverImage(s3UploadResponseCoverImageFile.getFileUrl());
        book.setCoverImageFileName(coverImageFile.getOriginalFilename());

        logger.info("Saving new book: " + book.getDescription());
        return bookRepository.save(book);
    }

    @Override
    public Book update(Long id, String name, String description, String author, Long subjectId, MultipartFile coverImageFile, MultipartFile file) throws BookNotFoundException, SubjectNotFoundException {
        Subject subject = subjectServiceImpl.findById(subjectId);
        logger.info("Name: " + name);
        Book existBook = findById(id);
        existBook.setSubject(subject);
        existBook.setName(name);
        existBook.setDescription(description);
        existBook.setAuthor(author);

        // Se um novo arquivo é fornecido, atualiza o arquivo no serviço Amazon S3 e atualiza o nome e a URL do arquivo
        if (file != null) {
            if (existBook.getFileName() != null) {
                logger.info("Deleting file: " + existBook.getFileName());
                amazonS3Service.deleteFile(existBook.getFileName(), BUCKET_NAME);
            }
            S3UploadResponse s3UploadResponse = amazonS3Service.uploadFile(file, BUCKET_NAME);
            existBook.setUrlFile(s3UploadResponse.getFileUrl());
            existBook.setFileName(file.getOriginalFilename());
        }

        // Se um novo arquivo é fornecido, atualiza o arquivo no serviço Amazon S3 e atualiza o nome e a URL do arquivo
        if (coverImageFile != null) {
            if (existBook.getCoverImageFileName() != null) {
                logger.info("Deleting file: " + existBook.getCoverImageFileName());
                amazonS3Service.deleteFile(existBook.getCoverImageFileName(), BUCKET_NAME);
            }
            S3UploadResponse s3UploadResponse = amazonS3Service.uploadFile(coverImageFile, BUCKET_NAME);
            existBook.setUrlCoverImage(s3UploadResponse.getFileUrl());
            existBook.setCoverImageFileName(coverImageFile.getOriginalFilename());
        }

        logger.info("Saving new book: " + existBook.getDescription());
        return bookRepository.save(existBook);
    }

    @Override
    public void delete(Long id) throws BookNotFoundException {
        Book existBook = findById(id);
        logger.info("Deleting book: " + existBook.getDescription());
        bookRepository.deleteById(id);
        if (existBook.getFileName() != null) {
            logger.info("Deleting file: " + existBook.getFileName());
            amazonS3Service.deleteFile(existBook.getFileName(), BUCKET_NAME);
        }
    }

    @Override
    public byte[] downloadFile(Long id, @PathVariable String fileName) throws BookNotFoundException {
        Book existBook = findById(id);
        logger.info("Downloading file: " + existBook.getFileName());
        byte[] data = amazonS3Service.downloadFile(fileName, BUCKET_NAME);
        existBook.setTotalDownloadNumber(existBook.getTotalDownloadNumber() + 1);
        bookRepository.save(existBook);
        return data;
    }

    @Override
    public long getTotal(){
        logger.info("Total books: " + bookRepository.count());
        return bookRepository.count();
    }
}