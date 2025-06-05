package com.fabiocondo.exception;

import com.fabiocondo.domain.HttpResponse;
import com.fabiocondo.exception.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.io.IOException;
import java.util.Objects;

import static org.springframework.http.HttpStatus.*;

@RestControllerAdvice
public class ExceptionHandling implements ErrorController {

    private final Logger LOGGER = LoggerFactory.getLogger(getClass());
    private static final String ACCOUNT_LOCKED = "Your account has been locked. Please contact administration";
    private static final String METHOD_IS_NOT_ALLOWED = "This request method is not allowed on this endpoint. Please send a '%s' request";
    private static final String INTERNAL_SERVER_ERROR_MSG = "An error occurred while processing the request";
    private static final String INCORRECT_CREDENTIALS = "Username / password incorrect. Please try again";
    private static final String ACCOUNT_DISABLED = "Your account has been disabled. If this is an error, please contact administration";
    private static final String ERROR_PROCESSING_FILE = "Error occurred while processing file";
    private static final String NOT_ENOUGH_PERMISSION = "You do not have enough permission";
    public static final String ERROR_PATH = "/error";
    private static final String ENTITY_IN_USE = "Entity in use, can't be deleted";

    @ExceptionHandler(ArticleNotFoundException.class)
    public ResponseEntity<HttpResponse> articleNotFoundException(ArticleNotFoundException exception) {
        return createHttpResponse(BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(OtpNotFoundException.class)
    public ResponseEntity<HttpResponse> otpNotFoundException(OtpNotFoundException exception) {
        return createHttpResponse(BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(OtpExpiredException.class)
    public ResponseEntity<HttpResponse> otpExpiredException(OtpExpiredException exception) {
        return createHttpResponse(BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(InvalidOtpException.class)
    public ResponseEntity<HttpResponse> invalidOtpException(InvalidOtpException exception) {
        return createHttpResponse(BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(EmailExistException.class)
    public ResponseEntity<HttpResponse> emailExistException(EmailExistException exception) {
        return createHttpResponse(BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(UsernameExistException.class)
    public ResponseEntity<HttpResponse> usernameExistException(UsernameExistException exception) {
        return createHttpResponse(BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(EmailNotFoundException.class)
    public ResponseEntity<HttpResponse> emailNotFoundException(EmailNotFoundException exception) {
        return createHttpResponse(BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(UserInvitedException.class)
    public ResponseEntity<HttpResponse> userInvitedException(UserInvitedException exception) {
        return createHttpResponse(BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<HttpResponse> userNotFoundException(UserNotFoundException exception) {
        return createHttpResponse(BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(TopicNotFoundException.class)
    public ResponseEntity<HttpResponse> topicNotFoundException(TopicNotFoundException exception) {
        return createHttpResponse(BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(ModuleNotFoundException.class)
    public ResponseEntity<HttpResponse> moduleNotFoundException(ModuleNotFoundException exception) {
        return createHttpResponse(BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(CourseNotFoundException.class)
    public ResponseEntity<HttpResponse> onlineCourseNotFoundException(CourseNotFoundException exception) {
        return createHttpResponse(BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(SubmissionNotFoundException.class)
    public ResponseEntity<HttpResponse> submissionNotFoundException(SubmissionNotFoundException exception) {
        return createHttpResponse(BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(CompetitionUserUnauthorizedExceptionException.class)
    public ResponseEntity<HttpResponse> competitionUserUnauthorizedExceptionException(CompetitionUserUnauthorizedExceptionException exception) {
        return createHttpResponse(BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(ClosedSubmissionException.class)
    public ResponseEntity<HttpResponse> closedSubmissionException(ClosedSubmissionException exception) {
        return createHttpResponse(BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(UserAlreadySubmittedException.class)
    public ResponseEntity<HttpResponse> userAlreadySubmittedException(UserAlreadySubmittedException exception) {
        return createHttpResponse(BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(CompetitionNotFoundException.class)
    public ResponseEntity<HttpResponse> competitionNotFoundException(CompetitionNotFoundException exception) {
        return createHttpResponse(BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(QuizNotFoundException.class)
    public ResponseEntity<HttpResponse> quizNotFoundException(QuizNotFoundException exception) {
        return createHttpResponse(BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(QuestionNotFoundException.class)
    public ResponseEntity<HttpResponse> questionNotFoundException(QuestionNotFoundException exception) {
        return createHttpResponse(BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(InterestNotFoundException.class)
    public ResponseEntity<HttpResponse> interestNotFoundException(InterestNotFoundException exception) {
        return createHttpResponse(BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(UserCourseNotFoundException.class)
    public ResponseEntity<HttpResponse> userCourseNotFoundException(UserCourseNotFoundException exception) {
        return createHttpResponse(BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(BookNotFoundException.class)
    public ResponseEntity<HttpResponse> bookNotFoundException(BookNotFoundException exception) {
        return createHttpResponse(BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(TeacherNotFoundException.class)
    public ResponseEntity<HttpResponse> teacherNotFoundException(TeacherNotFoundException exception) {
        return createHttpResponse(BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(SubjectNotFoundException.class)
    public ResponseEntity<HttpResponse> subjectNotFoundException(SubjectNotFoundException exception) {
        return createHttpResponse(BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(CombinationExistException.class)
    public ResponseEntity<HttpResponse> combinationExistException(CombinationExistException exception) {
        return createHttpResponse(BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<HttpResponse> badCredentialsException() {
        return createHttpResponse(BAD_REQUEST, INCORRECT_CREDENTIALS);
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<HttpResponse> accountDisabledException() {
        return createHttpResponse(BAD_REQUEST, ACCOUNT_DISABLED);
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<HttpResponse> lockedException() {
        return createHttpResponse(UNAUTHORIZED, ACCOUNT_LOCKED);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<HttpResponse> accessDeniedException() {
        return createHttpResponse(FORBIDDEN, NOT_ENOUGH_PERMISSION.toUpperCase());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<HttpResponse> illegalArgumentException(IllegalArgumentException exception) {
        return createHttpResponse(BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<HttpResponse> handleMissingServletRequestPartException(MissingServletRequestPartException exception) {
        LOGGER.error(exception.getMessage());
        return createHttpResponse(BAD_REQUEST, exception.getMessage().toUpperCase()); // REQUIRED REQUEST PART 'FILE' IS NOT PRESENT
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<HttpResponse> methodNotSupportedException(HttpRequestMethodNotSupportedException exception) {
        HttpMethod supportedMethod = Objects.requireNonNull(exception.getSupportedHttpMethods()).iterator().next();
        return createHttpResponse(METHOD_NOT_ALLOWED, String.format(METHOD_IS_NOT_ALLOWED, supportedMethod));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<HttpResponse> internalServerErrorException(Exception exception) {
        LOGGER.error(exception.getMessage());
        return createHttpResponse(INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR_MSG.toUpperCase());
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<HttpResponse> iOException(IOException exception) {
        LOGGER.error(exception.getMessage());
        return createHttpResponse(INTERNAL_SERVER_ERROR, ERROR_PROCESSING_FILE.toUpperCase());
    }

    @ExceptionHandler(EntityInUseException.class)
    public ResponseEntity<HttpResponse> handleEntityInUseException(EntityInUseException exception) {
        LOGGER.error(exception.getMessage());
        return createHttpResponse(CONFLICT, ENTITY_IN_USE.toUpperCase());
    }

    private ResponseEntity<HttpResponse> createHttpResponse(HttpStatus httpStatus, String message) {
        return new ResponseEntity<>(new HttpResponse(httpStatus.value(), httpStatus,
                httpStatus.getReasonPhrase().toUpperCase(), message), httpStatus);
    }

    @RequestMapping(ERROR_PATH)
    public ResponseEntity<HttpResponse> notFound404() {
        return createHttpResponse(NOT_FOUND, "There is no mapping for this URL".toUpperCase());
    }

    public String getErrorPath() {
        return ERROR_PATH;
    }
}
